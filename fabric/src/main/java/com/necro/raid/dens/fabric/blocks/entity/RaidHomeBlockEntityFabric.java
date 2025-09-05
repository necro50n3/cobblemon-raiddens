package com.necro.raid.dens.fabric.blocks.entity;

import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RaidHomeBlockEntityFabric extends RaidHomeBlockEntity {
    public RaidHomeBlockEntityFabric(BlockPos blockPos, BlockState blockState) {
        super(FabricBlocks.RAID_HOME_BLOCK_ENTITY, blockPos, blockState);
    }
}
