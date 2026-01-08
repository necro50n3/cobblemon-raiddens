package com.necro.raid.dens.common.worldgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.events.RaidDenSpawnEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.SetRaidBossEvent;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidCycleMode;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import com.necro.raid.dens.common.util.RaidUtils;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.minecraft.core.BlockPos;
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
        BlockPos blockPos = context.origin();
        BlockState blockState = context.config().state;

        if (level.isClientSide()) return false;
        else if (!RaidUtils.hasSkyAccess(level, blockPos)) return false;
        else if (!level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) return false;

        RaidCycleMode cycleMode = RaidCycleMode.fromString(CobblemonRaidDens.CONFIG.cycle_mode);
        ResourceLocation bucket = null;
        ResourceLocation location = null;
        RaidBoss raidBoss = null;

        if (cycleMode == RaidCycleMode.BUCKET && level.getServer() != null) {
            bucket = RaidBucketRegistry.getRandomBucket(level.getRandom(), level.getBiome(blockPos));
            if (bucket != null) {
                location = RaidBucketRegistry.getBucket(bucket).getRandomRaidBoss(level.getRandom(), level.getLevel());
                raidBoss = RaidRegistry.getRaidBoss(location);
            }
        }

        if (raidBoss == null) {
            location = RaidRegistry.getRandomRaidBoss(context.random(), level.getLevel());
            raidBoss = RaidRegistry.getRaidBoss(location);
        }
        if (raidBoss == null) return false;

        SetRaidBossEvent event = new SetRaidBossEvent(raidBoss);
        RaidEvents.SET_RAID_BOSS.emit(event);
        raidBoss = event.getRaidBoss();
        if (raidBoss == null) return false;

        level.setBlock(blockPos, blockState
            .setValue(RaidCrystalBlock.ACTIVE, true)
            .setValue(RaidCrystalBlock.CAN_RESET, true)
            .setValue(RaidCrystalBlock.CYCLE_MODE, RaidCycleMode.CONFIG)
            .setValue(RaidCrystalBlock.RAID_TYPE, raidBoss.getType())
            .setValue(RaidCrystalBlock.RAID_TIER, raidBoss.getTier()), 2);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof RaidCrystalBlockEntity raidCrystal)) return false;
        raidCrystal.setRaidBoss(location, level.getLevel().getGameTime());
        raidCrystal.setRaidBucket(bucket);

        RaidEvents.RAID_DEN_SPAWN.emit(new RaidDenSpawnEvent(level.getLevel(), blockPos, raidBoss));
        return true;
    }
}
