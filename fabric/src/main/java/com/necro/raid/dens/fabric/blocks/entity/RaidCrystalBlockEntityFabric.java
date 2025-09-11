package com.necro.raid.dens.fabric.blocks.entity;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RaidCrystalBlockEntityFabric extends RaidCrystalBlockEntity {
    public RaidCrystalBlockEntityFabric(BlockPos blockPos, BlockState blockState) {
        super(FabricBlocks.RAID_CRYSTAL_BLOCK_ENTITY, blockPos, blockState);
    }
}
