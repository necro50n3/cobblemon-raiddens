package com.necro.raid.dens.neoforge.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import com.necro.raid.dens.common.items.item.RaidHomeBlockItem;
import com.necro.raid.dens.neoforge.blocks.block.RaidCrystalBlockNeoForge;
import com.necro.raid.dens.neoforge.blocks.block.RaidHomeBlockNeoForge;
import com.necro.raid.dens.neoforge.items.NeoForgeItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeBlocks extends ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CobblemonRaidDens.MOD_ID);
    public static final DeferredBlock<?> RAID_CRYSTAL_BLOCK = registerBlock(
        "raid_crystal_block",
        () -> new RaidCrystalBlockNeoForge(ModBlocks.PROPERTIES),
        (block) -> () -> new RaidCrystalBlockItem(block.get(), new Item.Properties())
    );
    public static final DeferredBlock<?> RAID_HOME_BLOCK = registerBlock(
        "raid_home_block",
        () -> new RaidHomeBlockNeoForge(ModBlocks.HOME_BLOCK_PROPERTIES),
        (block) -> () -> new RaidHomeBlockItem(block.get(), new Item.Properties())
    );

    public static DeferredBlock<?> registerBlock(String name, Supplier<Block> block, Function<DeferredBlock<?>, Supplier<BlockItem>> itemFactory) {
        DeferredBlock<?> toReturn = BLOCKS.register(name, block);
        NeoForgeItems.registerBlockItem(name, itemFactory.apply(toReturn));
        return toReturn;
    }

    public static void registerModBlocks() {
        INSTANCE = new NeoForgeBlocks();
    }

    @Override
    public Block getRaidCrystalBlock() {
        return RAID_CRYSTAL_BLOCK.get();
    }

    @Override
    public Block getRaidHomeBlock() {
        return RAID_HOME_BLOCK.get();
    }
}
