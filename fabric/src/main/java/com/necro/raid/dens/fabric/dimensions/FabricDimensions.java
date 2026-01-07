package com.necro.raid.dens.fabric.dimensions;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.dimensions.RaidDenChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricDimensions {
    public static void registerChunkGenerator() {
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dimension_chunk"),
            RaidDenChunkGenerator.CODEC
        );
    }
}
