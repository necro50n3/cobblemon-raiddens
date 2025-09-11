package com.necro.raid.dens.neoforge.blocks.entity;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RaidCrystalBlockEntityNeoForge extends RaidCrystalBlockEntity {
    public RaidCrystalBlockEntityNeoForge(BlockPos blockPos, BlockState blockState) {
        super(NeoForgeBlockEntities.RAID_CRYSTAL_BLOCK_ENTITY.get(), blockPos, blockState);
    }
}
