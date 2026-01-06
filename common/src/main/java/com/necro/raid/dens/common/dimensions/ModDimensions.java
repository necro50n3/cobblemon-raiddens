package com.necro.raid.dens.common.dimensions;

import com.google.common.collect.ImmutableList;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.mixins.dimension.MappedRegistryAccessor;
import com.necro.raid.dens.common.mixins.dimension.MinecraftServerAccessor;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public class ModDimensions {
    public static final ResourceKey<DimensionType> RAID_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dimension_type"));

    public static final ResourceKey<Biome> RAID_DIM_BIOME = ResourceKey.create(Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den"));

    public static final ResourceKey<LevelStem> RAID_DIM = ResourceKey.create(Registries.LEVEL_STEM,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_dimension"));

    public static LevelStem raidDimBuilder(MinecraftServer server, ResourceKey<LevelStem> dimensionKey) {
        RegistryAccess registries = server.registryAccess();
        return new LevelStem(
            registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(RAID_DIM_TYPE),
            new RaidDenChunkGenerator(registries.registryOrThrow(Registries.BIOME).getHolderOrThrow(RAID_DIM_BIOME))
        );
    }

    public static ResourceKey<Level> createLevelKey(String uuid) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, uuid));
    }

    public static ResourceKey<Level> createLevelKey(ResourceLocation loc) {
        return ResourceKey.create(Registries.DIMENSION, loc);
    }

    /*
    Credits to SoulHome mod for method
     */
    public static ServerLevel createRaidDimension(MinecraftServer server, ResourceKey<Level> levelKey) {
        if (server.getLevel(levelKey) != null) return server.getLevel(levelKey);
        ResourceKey<LevelStem> dimKey = ResourceKey.create(Registries.LEVEL_STEM, levelKey.location());

        BiFunction<MinecraftServer, ResourceKey<LevelStem>, LevelStem> dimensionFactory = ModDimensions::raidDimBuilder;
        LevelStem dimension = dimensionFactory.apply(server, dimKey);

        Executor executor = ((MinecraftServerAccessor) server).getExecutor();
        LevelStorageSource.LevelStorageAccess levelSave = ((MinecraftServerAccessor) server).getStorageSource();
        ChunkProgressListener chunkProgressListener = ((MinecraftServerAccessor) server).getProgressListenerFactory().create(11);

        WorldData serverConfiguration = server.getWorldData();

        Registry<LevelStem> dimensionRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        if (dimensionRegistry instanceof WritableRegistry<LevelStem> writableRegistry) {
            boolean wasFrozen = ((MappedRegistryAccessor<?>) writableRegistry).getFrozen();
            ((MappedRegistryAccessor<?>) writableRegistry).setFrozen(false);
            writableRegistry.register(dimKey, dimension, RegistrationInfo.BUILT_IN);
            if (wasFrozen) ((MappedRegistryAccessor<?>) writableRegistry).setFrozen(true);
        }
        else throw new IllegalStateException("Unable to register dimension '" + dimKey.location() + "'! Registry not writable!");

        DerivedLevelData derivedWorldInfo = new DerivedLevelData(serverConfiguration, serverConfiguration.overworldData());

        ServerLevel raidDim = new ServerLevel(
            server,
            executor,
            levelSave,
            derivedWorldInfo,
            levelKey,
            dimension,
            chunkProgressListener,
            serverConfiguration.isDebugWorld(),
            BiomeManager.obfuscateSeed(serverConfiguration.worldGenOptions().seed()),
            ImmutableList.of(),
            false,
            null
        );

        ((MinecraftServerAccessor) server).getLevels().put(levelKey, raidDim);
        return raidDim;
    }

    public static void placeRaidDenStructure(RaidCrystalBlockEntity blockEntity, ServerLevel level) {
        StructureTemplateManager structureManager = level.getStructureManager();
        StructureTemplate template = structureManager.get(blockEntity.getRaidStructure()).orElseGet(() -> {
            blockEntity.setRaidStructure(RaidDenRegistry.DEFAULT);
            return structureManager.getOrCreate(blockEntity.getRaidStructure());
        });
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.clearProcessors();
        settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);

        Vec3 offset = RaidDenRegistry.getOffset(blockEntity.getRaidStructure());
        BlockPos corner = BlockPos.containing(offset);

        for (Entity e : level.getAllEntities()) {
            if (e != null) e.discard();
        }

        template.placeInWorld(level, corner, corner, settings, level.getRandom(), 2);

        level.setBlockAndUpdate(BlockPos.ZERO, ModBlocks.INSTANCE.getRaidHomeBlock().defaultBlockState());
        if (level.getBlockEntity(BlockPos.ZERO) instanceof RaidHomeBlockEntity homeBlockEntity) {
            homeBlockEntity.setHome(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel());
        }
    }
}
