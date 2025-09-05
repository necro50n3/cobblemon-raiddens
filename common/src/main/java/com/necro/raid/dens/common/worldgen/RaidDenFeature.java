package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

import java.util.Arrays;

public class RaidDenFeature extends Feature<BlockStateConfiguration> {
    public RaidDenFeature() {
        super(BlockStateConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        if (!CobblemonRaidDens.CONFIG.enable_spawning) return false;
        else if (Arrays.stream(RaidTier.values()).noneMatch(RaidTier::isPresent)) return false;
        else if (Arrays.stream(RaidType.values()).noneMatch(RaidType::isPresent)) return false;
        
        WorldGenLevel level = context.level();
        BlockPos blockPos = context.origin();
        BlockState blockState = context.config().state;

        if (level.isClientSide()) return false;
        else if (level.getLevel().canSeeSky(blockPos.above())) return false;
        else if (!level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) return false;

        RaidTier tier = RaidTier.getWeightedRandom(context.random());
        RaidBoss raidBoss = RaidRegistry.getRandomRaidBoss(context.random(), tier);
        if (raidBoss == null) return false;

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, CobblemonRaidDens.CONFIG.reset_time > 0)
            .setValue(RaidCrystalBlock.CAN_CYCLE, CobblemonRaidDens.CONFIG.can_cycle)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, tier), 2);

        ((RaidCrystalBlockEntity) level.getBlockEntity(blockPos)).setRaidBoss(raidBoss);

        return true;
    }
}
