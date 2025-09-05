package com.necro.raid.dens.neoforge.blocks.entity;

import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RaidHomeBlockEntityNeoForge extends RaidHomeBlockEntity {
    public RaidHomeBlockEntityNeoForge(BlockPos blockPos, BlockState blockState) {
        super(NeoForgeBlockEntities.RAID_HOME_BLOCK_ENTITY.get(), blockPos, blockState);
    }
}
