package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class ModFeatures {
    public static Holder<Feature<BlockStateConfiguration>> RAID_DEN_FEATURE;
    public static final TagKey<Biome> RAID_SPAWNABLE = TagKey.create(
        Registries.BIOME, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_spawnable")
    );
}
