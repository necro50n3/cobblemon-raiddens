package com.necro.raid.dens.common.blocks;

import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ModBlocks {
    public static BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
        .sound(SoundType.AMETHYST).strength(50.0f, 1200.0f).noLootTable()
        .noOcclusion().isValidSpawn(ModBlocks::never).isRedstoneConductor(ModBlocks::never)
        .isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never)
        .lightLevel(blockState -> blockState.getValue(RaidCrystalBlock.ACTIVE) ? 8 : 0);

    public static BlockBehaviour.Properties HOME_BLOCK_PROPERTIES = BlockBehaviour.Properties.of()
        .sound(SoundType.AMETHYST).strength(-1.0f, 3600000.0f).noLootTable()
        .noOcclusion().isValidSpawn(ModBlocks::never).isRedstoneConductor(ModBlocks::never)
        .isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never);

    private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Object object) {
        return false;
    }

    private static boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return false;
    }
}
