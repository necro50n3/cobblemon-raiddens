package com.necro.raid.dens.common.dimensions;

import com.google.common.collect.Maps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.distanthorizons.RaidDensDistantHorizonsCompat;
import com.necro.raid.dens.common.mixins.MinecraftServerAccessor;
import com.necro.raid.dens.common.mixins.ServerLevelAccessor;
import com.necro.raid.dens.common.util.ILevelsSetter;
import com.necro.raid.dens.common.util.IRegistryRemover;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DimensionHelper {
    public static TriConsumer<MinecraftServer, ResourceKey<Level>, Boolean> SYNC_DIMENSIONS;
    private static final Set<PendingDimension> QUEUED_FOR_REMOVAL = new HashSet<>();
    private static final Set<ResourceKey<Level>> DELAYED_REMOVAL = new HashSet<>();
    private static final Set<ResourceKey<Level>> REMOVED_LEVELS = new HashSet<>();

    public static void queueForRemoval(ResourceKey<Level> key, ServerLevel level) {
        QUEUED_FOR_REMOVAL.add(new PendingDimension(key, level));
    }

    public static void addToCache(ServerLevel level) {
        DELAYED_REMOVAL.add(level.dimension());
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof ServerPlayer) && entity != null) entity.discard();
        }
    }

    public static void removeFromCache(ResourceKey<Level> level) {
        DELAYED_REMOVAL.remove(level);
    }

    public static void removePending(MinecraftServer server) {
        if (QUEUED_FOR_REMOVAL.isEmpty()) return;

        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels = ((MinecraftServerAccessor) server).getLevels();
        LinkedHashMap<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> newLevels = Maps.newLinkedHashMap();
        for (Map.Entry<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> entry : levels.entrySet()) {
            if (QUEUED_FOR_REMOVAL.stream().anyMatch(pd -> pd.levelKey == entry.getKey())) continue;
            newLevels.put(entry.getKey(), entry.getValue());
        }
        ((ILevelsSetter) server).setLevels(newLevels);

        QUEUED_FOR_REMOVAL.forEach(pd -> {
            REMOVED_LEVELS.add(pd.levelKey);
            if (!pd.isRunning) pd.saveAndClose(server);
        });
        QUEUED_FOR_REMOVAL.clear();
    }

    public static void removeDelayed(MinecraftServer server, ServerPlayer player) {
        ResourceKey<Level> key = ModDimensions.createLevelKey(player.getStringUUID());
        if (!DELAYED_REMOVAL.contains(key)) return;
        ServerLevel level = server.getLevel(key);
        if (level == null) return;

        queueForRemoval(key, level);
        SYNC_DIMENSIONS.accept(server, key, false);
        removeFromCache(key);
    }

    public static void removeDelayed(MinecraftServer server) {
        for (ResourceKey<Level> key : DELAYED_REMOVAL) {
            ServerLevel level = server.getLevel(key);
            if (level == null) return;
            queueForRemoval(key, level);
        }
        removePending(server);
        DELAYED_REMOVAL.clear();
    }

    public static boolean isLevelRemovedOrPending(ResourceKey<Level> level) {
        return REMOVED_LEVELS.contains(level);
    }

    private static class PendingDimension {
        private final ResourceKey<Level> levelKey;
        private final ServerLevel level;
        private boolean isRunning;

        PendingDimension(ResourceKey<Level> levelKey, ServerLevel level) {
            this.levelKey = levelKey;
            this.level = level;
            this.isRunning = false;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void saveAndClose(MinecraftServer server) {
            this.isRunning = true;

            CompletableFuture.runAsync(() -> {
                try { ((ServerLevelAccessor) this.level).getEntityManager().close(); }
                catch (Throwable e) { CobblemonRaidDens.LOGGER.error("Error while closing entity manager: ", e); }
            }, Util.backgroundExecutor()).thenRunAsync(() -> {
                try {
                    this.level.getChunkSource().getLightEngine().close();
                    this.level.getChunkSource().chunkMap.close();

                    if (ModCompat.DISTANT_HORIZONS.isLoaded()) {
                        CompletableFuture.runAsync(
                            () -> RaidDensDistantHorizonsCompat.INSTANCE.unloadLevel(this.level),
                            Util.backgroundExecutor()
                        ).thenRun(() -> server.submit(() -> this.unregisterAndDelete(server)));
                    }
                    else server.submit(() -> this.unregisterAndDelete(server));
                }
                catch (Throwable e) { CobblemonRaidDens.LOGGER.error("Error while closing dimension: ", e); }
            }, server);
        }

        @SuppressWarnings("unchecked")
        private void unregisterAndDelete(MinecraftServer server) {
            MappedRegistry<LevelStem> levelStemRegistry = (MappedRegistry<LevelStem>) server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
            ResourceKey<LevelStem> resourceKey = ResourceKey.create(Registries.LEVEL_STEM, ModDimensions.createLevelKey(this.levelKey.location().getPath()).location());
            ((IRegistryRemover<LevelStem>) levelStemRegistry).getById().removeIf(holder -> holder.is(resourceKey));
            ((IRegistryRemover<LevelStem>) levelStemRegistry).removeDimension(this.levelKey.location());

            ((ILevelsSetter) server).deleteLevel(this.levelKey);
            CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> REMOVED_LEVELS.remove(this.level.dimension()));
        }
    }
}
