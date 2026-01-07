package com.necro.raid.dens.common.dimensions;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ModDimensions {
    public static final ResourceKey<DimensionType> RAID_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dimension_type"));

    public static final ResourceKey<Biome> RAID_DIM_BIOME = ResourceKey.create(Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den"));

    public static final ResourceKey<LevelStem> RAID_DIM = ResourceKey.create(Registries.LEVEL_STEM,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dimension"));

    public static final ResourceKey<Level> RAID_DIMENSION = ResourceKey.create(Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dimension"));

    public static ServerLevel getRaidDimension(MinecraftServer server) {
        if (server == null) return null;
        return server.getLevel(RAID_DIMENSION);
    }
}
