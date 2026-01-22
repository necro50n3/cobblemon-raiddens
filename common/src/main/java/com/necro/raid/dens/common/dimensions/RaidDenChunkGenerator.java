package com.necro.raid.dens.common.dimensions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RaidDenChunkGenerator extends ChunkGenerator {
    public static final MapCodec<RaidDenChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(RegistryOps.retrieveElement(ModDimensions.RAID_DIM_BIOME)).apply(instance, instance.stable(RaidDenChunkGenerator::new))
    );

    public RaidDenChunkGenerator(Holder.Reference<Biome> biome) {
        super(new FixedBiomeSource(biome));
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(@NotNull WorldGenRegion worldGenRegion, long l, @NotNull RandomState randomState, @NotNull BiomeManager biomeManager, @NotNull StructureManager structureManager, @NotNull ChunkAccess chunkAccess, GenerationStep.@NotNull Carving carving) {

    }

    @Override
    public void buildSurface(@NotNull WorldGenRegion worldGenRegion, @NotNull StructureManager structureManager, @NotNull RandomState randomState, @NotNull ChunkAccess chunkAccess) {

    }

    @Override
    public void spawnOriginalMobs(@NotNull WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 128;
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(@NotNull Blender blender, @NotNull RandomState randomState, @NotNull StructureManager structureManager, @NotNull ChunkAccess chunkAccess) {
        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return -64;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.@NotNull Types types, @NotNull LevelHeightAccessor levelHeightAccessor, @NotNull RandomState randomState) {
        return 0;
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int i, int j, @NotNull LevelHeightAccessor levelHeightAccessor, @NotNull RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(@NotNull List<String> list, @NotNull RandomState randomState, @NotNull BlockPos blockPos) {

    }
}
