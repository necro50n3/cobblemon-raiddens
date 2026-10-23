package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.events.RaidDenSpawnEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.SetRaidEvent;
import com.necro.raid.dens.common.data.raid.RaidCycleMode;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import com.necro.raid.dens.common.registry.RaidRegistry;
import kotlin.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class RaidDenFeature extends Feature<BlockStateConfiguration> {
    public RaidDenFeature() {
        super(BlockStateConfiguration.CODEC);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<BlockStateConfiguration> context) {
        if (!CobblemonRaidDens.CONFIG.enable_spawning) return false;
        else if (Arrays.stream(RaidTier.values()).noneMatch(RaidTier::isPresent)) return false;
        else if (Arrays.stream(RaidType.values()).noneMatch(RaidType::isPresent)) return false;
        
        WorldGenLevel level = context.level();
        BlockState blockState = context.config().state;

        String dimension = level.getLevel().dimension().location().toString();
        int chance = CobblemonRaidDens.CONFIG.dimension_spawn_rate.getOrDefault(dimension, 0);
        if (chance < 1) return false;
        if (level.getRandom().nextInt(chance) != 0) return false;

        if (level.isClientSide()) return false;
        BlockPos blockPos = this.checkAdditionalRequirements(level, context.origin());
        if (blockPos == null) return false;

        boolean isAdventure = context.random().nextDouble() < CobblemonRaidDens.CONFIG.adventure_spawn_rate;
        ResourceLocation location = isAdventure ? this.generateAdventure(context, level, blockState, blockPos) : this.generateBoss(context, level, blockState, blockPos);
        if (location == null) return false;

        RaidRegistry.requestRaidSession(location, session -> {
            SetRaidEvent event = new SetRaidEvent(session);
            RaidEvents.SET_RAID.emit(event);
            session = event.getRaid();
            if (session == null) {
                this.handleError(level, blockPos, null);
                return;
            }

            RaidEvents.RAID_DEN_SPAWN.emit(new RaidDenSpawnEvent(level.getLevel(), blockPos, session));
        }, (id, error) -> handleError(level, blockPos, id), level.getServer());

        return true;
    }

    private @Nullable ResourceLocation generateAdventure(@NotNull FeaturePlaceContext<BlockStateConfiguration> context, WorldGenLevel level, BlockState blockState, BlockPos blockPos) {
        ResourceLocation location = CustomRaidRegistries.ADVENTURE_REGISTRY.getRandom(context.random());
        if (location == null) return null;

        Pair<RaidTier, RaidType> tierAndType = CustomRaidRegistries.ADVENTURE_REGISTRY.getTierAndType(location);
        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.IS_ADVENTURE, true)
            .setValue(RaidCrystalBlock.RAID_TYPE, tierAndType.getSecond())
            .setValue(RaidCrystalBlock.RAID_TIER, tierAndType.getFirst()), 2);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return null;
        raidCrystal.setRaidSession(location, level.getLevel().getGameTime());

        return location;
    }

    private @Nullable ResourceLocation generateBoss(@NotNull FeaturePlaceContext<BlockStateConfiguration> context, WorldGenLevel level, BlockState blockState, BlockPos blockPos) {
        RaidCycleMode cycleMode = CobblemonRaidDens.CONFIG.cycle_mode;
        ResourceLocation bucket = null;
        ResourceLocation location = null;

        if (cycleMode == RaidCycleMode.BUCKET) {
            bucket = RaidBucketRegistry.getRandomBucket(level.getRandom(), level.getBiome(blockPos));
            if (bucket != null) location = RaidBucketRegistry.getBucket(bucket).getRandomRaidBoss(level.getRandom(), level.getLevel());
        }

        if (!RaidRegistry.exists(location)) location = RaidRegistry.getRandomRaidBoss(context.random(), level.getLevel());
        if (!RaidRegistry.exists(location)) return null;

        Pair<RaidTier, RaidType> tierAndType = RaidRegistry.getTierAndType(location);
        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.IS_ADVENTURE, false)
            .setValue(RaidCrystalBlock.RAID_TYPE, tierAndType.getSecond())
            .setValue(RaidCrystalBlock.RAID_TIER, tierAndType.getFirst()), 2);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return null;
        raidCrystal.setRaidSession(location, level.getLevel().getGameTime());
        raidCrystal.setRaidBucket(bucket);

        return location;
    }

    private void handleError(WorldGenLevel level, BlockPos blockPos, ResourceLocation boss) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity crystal)) return;
        if (boss == null || !boss.equals(crystal.getRaidBossLocation())) return;
        level.setBlock(blockPos, crystal.getBlockState().setValue(RaidCrystalBlock.ACTIVE, false), 2);
    }

    protected BlockPos checkAdditionalRequirements(WorldGenLevel level, BlockPos blockPos) {
        if (!level.isEmptyBlock(blockPos)) return null;
        else if (!level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos.below(), Direction.UP)) return null;
        return blockPos;
    }
}
