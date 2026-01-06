package com.necro.raid.dens.fabricgen;

import com.necro.raid.dens.fabricgen.datagen.*;
import com.necro.raid.dens.fabricgen.dimensions.RaidDenDimensions;
import com.necro.raid.dens.fabricgen.worldgen.RaidDenConfiguredFeatures;
import com.necro.raid.dens.fabricgen.worldgen.RaidDenPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.dimension.LevelStem;

public class CobblemonRaidDensDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        DynamicRegistries.register(Registries.LEVEL_STEM, LevelStem.CODEC);
        pack.addProvider(WorldGenerator::new);
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(LootTableGenerator::new);
        pack.addProvider(AdvancementGenerator::new);
        pack.addProvider(BlockTagGenerator::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.BIOME, RaidDenDimensions::bootstrapBiome);
        registryBuilder.add(Registries.DIMENSION_TYPE, RaidDenDimensions::bootstrapType);
        registryBuilder.add(Registries.LEVEL_STEM, RaidDenDimensions::bootstrapLevel);
        registryBuilder.add(Registries.CONFIGURED_FEATURE, RaidDenConfiguredFeatures::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, RaidDenPlacedFeatures::bootstrap);
    }
}
