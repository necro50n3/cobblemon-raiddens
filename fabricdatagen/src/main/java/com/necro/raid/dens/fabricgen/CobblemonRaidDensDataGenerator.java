package com.necro.raid.dens.fabricgen;

import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.fabricgen.datagen.ItemTagGenerator;
import com.necro.raid.dens.fabricgen.datagen.LootTableGenerator;
import com.necro.raid.dens.fabricgen.datagen.WorldGenerator;
import com.necro.raid.dens.fabricgen.worldgen.RaidDenConfiguredFeatures;
import com.necro.raid.dens.fabricgen.worldgen.RaidDenPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class CobblemonRaidDensDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(WorldGenerator::new);
        pack.addProvider(ItemTagGenerator::new);
        pack.addProvider(LootTableGenerator::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.BIOME, ModDimensions::bootstrapBiome);
        registryBuilder.add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType);
        registryBuilder.add(Registries.CONFIGURED_FEATURE, RaidDenConfiguredFeatures::bootstrap);
        registryBuilder.add(Registries.PLACED_FEATURE, RaidDenPlacedFeatures::bootstrap);
    }
}
