package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidCycleMode;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import com.necro.raid.dens.common.util.RaidUtils;
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
        else if (!RaidUtils.hasSkyAccess(level, blockPos)) return false;
        else if (!level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) return false;

        RaidBoss raidBoss = RaidRegistry.getRandomRaidBoss(context.random(), level.getLevel());
        if (raidBoss == null) return false;

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, CobblemonRaidDens.CONFIG.reset_time > 0)
            .setValue(RaidCrystalBlock.CYCLE_MODE, RaidCycleMode.fromString(CobblemonRaidDens.CONFIG.cycle_mode))
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        ((RaidCrystalBlockEntity) level.getBlockEntity(blockPos)).setRaidBoss(raidBoss);

        return true;
    }
}
