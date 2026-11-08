package com.necro.raid.dens.fabricgen.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.worldgen.ModFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class RaidDenConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAID_DEN_KEY = registerKey("raid_den");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAID_DEN_IGNORE_SKY_KEY = registerKey("raid_den_ignore_sky");

    public static final ResourceKey<ConfiguredFeature<?, ?>> RAIDITE_KEY = registerKey("raidite_ore");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        register(context, RAID_DEN_KEY, ModFeatures.RAID_DEN_FEATURE.value(),
            new BlockStateConfiguration(ModBlocks.RAID_CRYSTAL_BLOCK.value().defaultBlockState())
        );
        register(context, RAID_DEN_IGNORE_SKY_KEY, ModFeatures.RAID_DEN_FEATURE_IGNORE_SKY.value(),
            new BlockStateConfiguration(ModBlocks.RAID_CRYSTAL_BLOCK.value().defaultBlockState())
        );

        RuleTest raiditeReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateRaiditeReplaceable = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> raiditeOres = List.of(
            OreConfiguration.target(raiditeReplaceable, ModBlocks.RAIDITE_ORE.value().defaultBlockState()),
            OreConfiguration.target(deepslateRaiditeReplaceable, ModBlocks.DEEPSLATE_RAIDITE_ORE.value().defaultBlockState())
        );

        register(context, RAIDITE_KEY, Feature.ORE, new OreConfiguration(raiditeOres, 8, 0.5F));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(
            Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name)
        );
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }
}
