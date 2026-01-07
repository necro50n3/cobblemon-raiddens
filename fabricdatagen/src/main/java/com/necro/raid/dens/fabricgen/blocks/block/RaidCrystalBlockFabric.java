package com.necro.raid.dens.fabricgen.blocks.block;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.fabricgen.blocks.FabricBlocks;
import com.necro.raid.dens.fabricgen.blocks.entity.RaidCrystalBlockEntityFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class RaidCrystalBlockFabric extends RaidCrystalBlock {
    public static final MapCodec<RaidCrystalBlockFabric> CODEC = simpleCodec(RaidCrystalBlockFabric::new);

    public RaidCrystalBlockFabric(Properties settings) {
        super(settings);
    }

    @Override
    protected @NotNull MapCodec<RaidCrystalBlockFabric> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new RaidCrystalBlockEntityFabric(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, FabricBlocks.RAID_CRYSTAL_BLOCK_ENTITY,
            (level2, blockPos, blockState2, blockEntity) -> blockEntity.tick(level2, blockPos, blockState2)
        );
    }
}
