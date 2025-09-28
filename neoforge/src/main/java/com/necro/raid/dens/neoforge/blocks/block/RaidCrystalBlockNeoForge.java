package com.necro.raid.dens.neoforge.blocks.block;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.neoforge.blocks.entity.RaidCrystalBlockEntityNeoForge;
import com.necro.raid.dens.neoforge.dimensions.NeoForgeDimensions;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RaidCrystalBlockNeoForge extends RaidCrystalBlock {
    public static final MapCodec<RaidCrystalBlockNeoForge> CODEC = simpleCodec(RaidCrystalBlockNeoForge::new);

    public RaidCrystalBlockNeoForge(Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull MapCodec<RaidCrystalBlockNeoForge> codec() {
        return CODEC;
    }

    @Override
    protected ServerLevel createDimension(RaidCrystalBlockEntity blockEntity) {
        return NeoForgeDimensions.createRaidDimension(blockEntity);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RaidCrystalBlockEntityNeoForge(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, NeoForgeBlockEntities.RAID_CRYSTAL_BLOCK_ENTITY.get(),
            (level2, blockPos, blockState2, blockEntity) -> blockEntity.tick(level2, blockPos, blockState2)
        );
    }
}
