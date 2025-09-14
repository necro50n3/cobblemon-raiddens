package com.necro.raid.dens.common.dimensions;

import com.google.common.collect.Maps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.distanthorizons.RaidDensDistantHorizonsCompat;
import com.necro.raid.dens.common.mixins.MinecraftServerAccessor;
import com.necro.raid.dens.common.mixins.ServerLevelAccessor;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.ILevelsSetter;
import com.necro.raid.dens.common.util.IRegistryRemover;
import net.minecraft.Util;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DimensionHelper {
    public static TriConsumer<MinecraftServer, ResourceKey<Level>, Boolean> SYNC_DIMENSIONS;
    private static final Set<PendingDimension> QUEUED_FOR_REMOVAL = new HashSet<>();

    public static void queueForRemoval(ResourceKey<Level> key, ServerLevel level) {
        QUEUED_FOR_REMOVAL.add(new PendingDimension(key, level));
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

        QUEUED_FOR_REMOVAL.forEach(pd -> { if (!pd.isRunning) pd.saveAndCLose(server); });
        QUEUED_FOR_REMOVAL.clear();
    }

    public static void onDimensionChange(ServerPlayer player, ServerLevel from, ServerLevel to) {
        boolean entering = isCustomDimension(to);
        boolean leaving = isCustomDimension(from);

        if (entering) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL) {
                player.setGameMode(GameType.ADVENTURE);
                RaidHelper.addSurvivalPlayer(player);
            }
        }
        else if (leaving) {
            if (RaidHelper.playerWasSurvival(player)) {
                player.setGameMode(GameType.SURVIVAL);
            }
        }
    }

    public static void onDimensionChange(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        if (player.getServer() == null) return;
        ServerLevel fromLevel = player.getServer().getLevel(from);
        ServerLevel toLevel = player.getServer().getLevel(to);
        onDimensionChange(player, fromLevel, toLevel);
    }

    public static boolean isCustomDimension(ServerLevel level) {
        return level.dimensionTypeRegistration().is(ModDimensions.RAIDDIM_TYPE);
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

        void saveAndCLose(MinecraftServer server) {
            this.isRunning = true;
            try {
                ((ServerLevelAccessor) this.level).getEntityManager().close();
                this.level.getChunkSource().getLightEngine().close();
                this.level.getChunkSource().chunkMap.close();

                if (ModCompat.DISTANT_HORIZONS.isLoaded()) {
                    CompletableFuture.runAsync(
                        () -> RaidDensDistantHorizonsCompat.INSTANCE.unloadLevel(this.level),
                        Util.backgroundExecutor()
                    ).thenRun(() -> this.unregisterAndDelete(server));
                }
                else this.unregisterAndDelete(server);
            }
            catch (Throwable e) {
                CobblemonRaidDens.LOGGER.error("Error while closing dimension: ", e);
            }
        }

        @SuppressWarnings("unchecked")
        private void unregisterAndDelete(MinecraftServer server) {
            MappedRegistry<LevelStem> levelStemRegistry = (MappedRegistry<LevelStem>) server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
            ResourceKey<LevelStem> resourceKey = ResourceKey.create(Registries.LEVEL_STEM, ModDimensions.createLevelKey(this.levelKey.location().getPath()).location());
            ((IRegistryRemover<LevelStem>) levelStemRegistry).getById().removeIf(holder -> holder.is(resourceKey));
            ((IRegistryRemover<LevelStem>) levelStemRegistry).removeDimension(this.levelKey.location());

            ((ILevelsSetter) server).deleteLevel(this.levelKey);
        }
    }
}
