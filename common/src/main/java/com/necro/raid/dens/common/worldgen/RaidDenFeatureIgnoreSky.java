package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.blocks.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;

public class RaidDenFeatureIgnoreSky extends RaidDenFeature {
    @Override
    protected BlockPos checkAdditionalRequirements(WorldGenLevel level, BlockPos blockPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        BlockPos targetPos = findFirstAirBlockAboveGround(level, mutableBlockPos);
        if (targetPos == null) return null;
        else if (level.getBlockState(targetPos.below()).is(BlockTags.RAID_SPAWNABLE_BLACKLIST)) return null;

        BlockPos.MutableBlockPos mutableBlockPos1 = new BlockPos.MutableBlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        for (int i = 0; i < 3; i++) {
            mutableBlockPos1.move(0, 1, 0);
            if (!level.isEmptyBlock(mutableBlockPos1)) return null;
        }

        return super.checkAdditionalRequirements(level, targetPos);
    }

    private static BlockPos findFirstAirBlockAboveGround(LevelAccessor level, BlockPos.MutableBlockPos mutableBlockPos) {
        do {
            mutableBlockPos.move(0, -1, 0);
            if (level.isOutsideBuildHeight(mutableBlockPos)) return null;
        }
        while(level.isEmptyBlock(mutableBlockPos) || level.getBlockState(mutableBlockPos).is(BlockTags.RAID_SPAWNABLE_BLACKLIST));

        mutableBlockPos.move(0, 1, 0);
        return mutableBlockPos.immutable();
    }
}
