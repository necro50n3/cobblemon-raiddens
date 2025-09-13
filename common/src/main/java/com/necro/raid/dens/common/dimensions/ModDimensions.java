package com.necro.raid.dens.common.dimensions;

import com.google.common.collect.ImmutableList;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.mixins.MappedRegistryAccessor;
import com.necro.raid.dens.common.mixins.MinecraftServerAccessor;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

import java.util.OptionalLong;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public class ModDimensions {
    public static final ResourceKey<DimensionType> RAIDDIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raiddim_type"));

    public static final ResourceKey<Biome> RAIDDIM_BIOME = ResourceKey.create(Registries.BIOME,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den"));

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(RAIDDIM_TYPE, new DimensionType(
            OptionalLong.of(12000),
            false,
            false,
            false,
            true,
            1.0,
            true,
            false,
            -64,
            128,
            128,
            BlockTags.AIR,
            BuiltinDimensionTypes.OVERWORLD_EFFECTS,
            1.0f,
            new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 0), 0)
        ));
    }

    public static void bootstrapBiome(BootstrapContext<Biome> context) {
        BiomeSpecialEffects.Builder builder = (new BiomeSpecialEffects.Builder())
            .waterColor(4159204)
            .waterFogColor(329011)
            .fogColor(12638463)
            .skyColor(0)
            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS);

        context.register(RAIDDIM_BIOME, new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .downfall(0.4f)
            .temperature(0.7f)
            .generationSettings(BiomeGenerationSettings.EMPTY)
            .mobSpawnSettings(MobSpawnSettings.EMPTY)
            .specialEffects(builder.build())
            .build()
        );
    }

    public static LevelStem raidDimBuilder(MinecraftServer server, ResourceKey<LevelStem> dimensionKey) {
        RegistryAccess registries = server.registryAccess();
        return new LevelStem(
            registries.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(RAIDDIM_TYPE),
            new RaidDenChunkGenerator(registries.registryOrThrow(Registries.BIOME).getHolderOrThrow(RAIDDIM_BIOME))
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

        ResourceLocation structure = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den");
        StructureTemplate template = raidDim.getStructureManager().getOrCreate(structure);
        StructurePlaceSettings settings = new StructurePlaceSettings();
        BlockPos corner = new BlockPos(-24, -3, -29);
        template.placeInWorld(raidDim, corner, corner, settings, raidDim.getRandom(), 2);

        return raidDim;
    }
}
