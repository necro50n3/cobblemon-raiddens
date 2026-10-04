package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.events.RaidDenSpawnEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.SetRaidBossEvent;
import com.necro.raid.dens.common.data.raid.RaidCycleMode;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
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

        RaidCycleMode cycleMode = CobblemonRaidDens.CONFIG.cycle_mode;
        ResourceLocation bucket = null;
        ResourceLocation location = null;

        if (cycleMode == RaidCycleMode.BUCKET) {
            bucket = RaidBucketRegistry.getRandomBucket(level.getRandom(), level.getBiome(blockPos));
            if (bucket != null) {
                location = RaidBucketRegistry.getBucket(bucket).getRandomRaidBoss(level.getRandom(), level.getLevel());
            }
        }

        if (!RaidRegistry.exists(location)) {
            location = RaidRegistry.getRandomRaidBoss(context.random(), level.getLevel());
        }
        if (!RaidRegistry.exists(location)) return false;

        Pair<RaidTier, RaidType> tierAndType = RaidRegistry.getTierAndType(location);
        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.RAID_TYPE, tierAndType.getSecond())
            .setValue(RaidCrystalBlock.RAID_TIER, tierAndType.getFirst()), 2);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return false;
        raidCrystal.setRaidBoss(location, level.getLevel().getGameTime());
        raidCrystal.setRaidBucket(bucket);

        RaidRegistry.requestRaidBoss(location, boss -> {
            SetRaidBossEvent event = new SetRaidBossEvent(boss);
            RaidEvents.SET_RAID_BOSS.emit(event);
            boss = event.getRaidBoss();
            if (boss == null) {
                this.handleError(level, blockPos, null);
                return;
            }

            RaidEvents.RAID_DEN_SPAWN.emit(new RaidDenSpawnEvent(level.getLevel(), blockPos, boss));
        }, (id, error) -> handleError(level, blockPos, id), level.getServer());

        return true;
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
