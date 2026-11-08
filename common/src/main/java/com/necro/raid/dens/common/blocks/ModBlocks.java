package com.necro.raid.dens.common.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.block.RaidHomeBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ModBlocks {
    public static ModBlocks INSTANCE;

    public static Holder<RaidCrystalBlock> RAID_CRYSTAL_BLOCK;
    public static Holder<RaidHomeBlock> RAID_HOME_BLOCK;
    public static Holder<Block> RAIDITE_ORE;
    public static Holder<Block> DEEPSLATE_RAIDITE_ORE;

    public static Holder<BlockEntityType<RaidCrystalBlockEntity>> RAID_CRYSTAL_BLOCK_ENTITY;
    public static Holder<BlockEntityType<RaidHomeBlockEntity>> RAID_HOME_BLOCK_ENTITY;

    public static final BlockBehaviour.Properties PROPERTIES = BlockBehaviour.Properties.of()
        .sound(SoundType.AMETHYST).strength(CobblemonRaidDens.CONFIG.can_break ? 3.0f : -1.0f, 3600000.0f)
        .noOcclusion().isValidSpawn(ModBlocks::never).isRedstoneConductor(ModBlocks::never)
        .isSuffocating(ModBlocks::never).isViewBlocking(ModBlocks::never)
        .lightLevel(blockState -> blockState.getValue(RaidCrystalBlock.ACTIVE) ? 8 : 0);

    public static final BlockBehaviour.Properties HOME_BLOCK_PROPERTIES = BlockBehaviour.Properties.of()
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
