package com.necro.raid.dens.common.dimensions;

import com.google.common.collect.Maps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.mixins.MinecraftServerAccessor;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.ILevelsSetter;
import com.necro.raid.dens.common.util.IRegistryRemover;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DimensionHelper {
    public static final HashMap<ResourceKey<Level>, ServerLevel> QUEUED_FOR_REMOVAL = new HashMap<>();

    public static void queueForRemoval(ResourceKey<Level> key, ServerLevel level) {
        QUEUED_FOR_REMOVAL.put(key, level);
    }

    public static void removePending(MinecraftServer server) {
        if (QUEUED_FOR_REMOVAL.isEmpty()) return;

        Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> levels = ((MinecraftServerAccessor) server).getLevels();
        LinkedHashMap<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> newLevels = Maps.newLinkedHashMap();
        for (Map.Entry<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> entry : levels.entrySet()) {
            if (QUEUED_FOR_REMOVAL.containsKey(entry.getKey())) continue;
            newLevels.put(entry.getKey(), entry.getValue());
        }
        ((ILevelsSetter) server).setLevels(newLevels);

        QUEUED_FOR_REMOVAL.forEach((key, level) -> {
            level.save(null, true, false);
            try { level.close(); }
            catch (IOException ignored) {}
            MappedRegistry<LevelStem> levelStemRegistry = (MappedRegistry<LevelStem>) server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);

            ResourceKey<LevelStem> resourceKey = ResourceKey.create(Registries.LEVEL_STEM, ModDimensions.createLevelKey(key.location().getPath()).location());
            ((IRegistryRemover<LevelStem>) levelStemRegistry).getById().removeIf(holder -> holder.is(resourceKey));
            ((IRegistryRemover<LevelStem>) levelStemRegistry).removeDimension(key.location());
            ((ILevelsSetter) server).deleteLevel(key);
        });
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

    private static boolean isCustomDimension(ServerLevel level) {
        DimensionType type = level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).get(ModDimensions.RAIDDIM_TYPE);
        return type != null && type == level.dimensionType();
    }
}
