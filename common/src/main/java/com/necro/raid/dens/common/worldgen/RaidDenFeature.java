package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.data.raid.RaidCycleMode;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
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
        if (level.isClientSide()) return false;

        String dimension = level.getLevel().dimension().location().toString();
        int chance = CobblemonRaidDens.CONFIG.dimension_spawn_rate.getOrDefault(dimension, 0);
        if (chance < 1) return false;
        if (level.getRandom().nextInt(chance) != 0) return false;

        BlockPos blockPos = this.checkAdditionalRequirements(level, context.origin());
        if (blockPos == null) return false;

        BlockState blockState = context.config().state;
        RaidCycleMode cycleMode = CobblemonRaidDens.CONFIG.cycle_mode;
        // ensure raid tier is random if it can't be cycled.
        if (!cycleMode.canCycleTier()) blockState = blockState.setValue(RaidCrystalBlock.RAID_TIER, RaidTier.getWeightedRandom(level.getRandom(), level.getLevel()));
        // ensure raid type is random if it can't be cycled.
        if (!cycleMode.canCycleType()) blockState = blockState.setValue(RaidCrystalBlock.RAID_TYPE, RaidType.getRandom(level.getRandom()));

        level.setBlock(blockPos, blockState, 2);
        return true;
    }

    protected BlockPos checkAdditionalRequirements(WorldGenLevel level, BlockPos blockPos) {
        if (!level.isEmptyBlock(blockPos)) return null;
        else if (!level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos.below(), Direction.UP)) return null;
        return blockPos;
    }
}
