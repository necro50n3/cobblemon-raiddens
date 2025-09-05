package com.necro.raid.dens.common.util;

import com.google.common.collect.Maps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.mixins.MinecraftServerAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

import java.io.IOException;
import java.util.*;

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
            Registry<LevelStem> levelStemRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
            ((IRegistryRemover) levelStemRegistry).removeDimension(key.location());
            ((ILevelsSetter) server).deleteLevel(key);
        });
        QUEUED_FOR_REMOVAL.clear();
    }
}
