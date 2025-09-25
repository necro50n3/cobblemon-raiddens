package com.necro.raid.dens.neoforge.dimensions;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.dimensions.RaidDenChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class NeoForgeChunkGenerator {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, CobblemonRaidDens.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<RaidDenChunkGenerator>> CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("raiddim_chunk", () -> RaidDenChunkGenerator.CODEC);
}
