package com.necro.raid.dens.fabricgen.dimensions;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.dimensions.RaidDenChunkGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class FabricDimensions {
    public static void registerChunkGenerator() {
        Registry.register(
            BuiltInRegistries.CHUNK_GENERATOR,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dim_chunk"),
            RaidDenChunkGenerator.CODEC
        );
    }
}
