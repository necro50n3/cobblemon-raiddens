package com.necro.raid.dens.fabricgen.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class RaidDenPlacedFeatures {
    public static final ResourceKey<PlacedFeature> RAID_DEN_PLACED_KEY = registerKey("raid_den_placed");
    public static final ResourceKey<PlacedFeature> RAID_DEN_PLACED_IGNORE_SKY_KEY = registerKey("raid_den_placed_ignore_sky");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, RAID_DEN_PLACED_KEY,
            configuredFeatures.getOrThrow(RaidDenConfiguredFeatures.RAID_DEN_KEY),
            InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            BiomeFilter.biome()
        );

        register(context, RAID_DEN_PLACED_IGNORE_SKY_KEY,
            configuredFeatures.getOrThrow(RaidDenConfiguredFeatures.RAID_DEN_IGNORE_SKY_KEY),
            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(30), VerticalAnchor.belowTop(30)),
            BiomeFilter.biome()
        );
    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(
            Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name)
        );
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> config, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(config, List.copyOf(modifiers)));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> config, PlacementModifier... modifiers) {
        register(context, key, config, List.of(modifiers));
    }
}
