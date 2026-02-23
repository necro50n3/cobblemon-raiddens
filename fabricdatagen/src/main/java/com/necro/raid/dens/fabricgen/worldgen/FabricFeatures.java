package com.necro.raid.dens.fabricgen.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.worldgen.ModFeatures;
import com.necro.raid.dens.common.worldgen.RaidDenFeature;
import com.necro.raid.dens.common.worldgen.RaidDenFeatureIgnoreSky;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;

public class FabricFeatures {
    public static void registerFeatures() {
        ModFeatures.RAID_DEN_FEATURE = Holder.direct(Registry.register(BuiltInRegistries.FEATURE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_feature"),
            new RaidDenFeature()
        ));
        ModFeatures.RAID_DEN_FEATURE_IGNORE_SKY = Holder.direct(Registry.register(BuiltInRegistries.FEATURE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_feature_ignore_sky"),
            new RaidDenFeatureIgnoreSky()
        ));
        addToBiome();
    }

    private static void addToBiome() {
        BiomeModifications.addFeature(
            BiomeSelectors.tag(ModFeatures.RAID_SPAWNABLE),
            GenerationStep.Decoration.SURFACE_STRUCTURES,
            ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_placed"))
        );

        BiomeModifications.addFeature(
            BiomeSelectors.tag(ModFeatures.RAID_SPAWNABLE_IGNORE_SKY),
            GenerationStep.Decoration.VEGETAL_DECORATION,
            ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_placed_ignore_sky"))
        );
    }
}
