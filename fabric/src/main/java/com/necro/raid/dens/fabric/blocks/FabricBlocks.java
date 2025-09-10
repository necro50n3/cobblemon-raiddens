package com.necro.raid.dens.fabric.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import com.necro.raid.dens.common.items.item.RaidHomeBlockItem;
import com.necro.raid.dens.fabric.blocks.block.RaidHomeBlockFabric;
import com.necro.raid.dens.fabric.blocks.entity.RaidCrystalBlockEntityFabric;
import com.necro.raid.dens.fabric.blocks.block.RaidCrystalBlockFabric;
import com.necro.raid.dens.fabric.blocks.entity.RaidHomeBlockEntityFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public class FabricBlocks extends ModBlocks {
    public static Block RAID_CRYSTAL_BLOCK;
    public static Block RAID_HOME_BLOCK;
    public static BlockEntityType<RaidCrystalBlockEntityFabric> RAID_CRYSTAL_BLOCK_ENTITY;
    public static BlockEntityType<RaidHomeBlockEntityFabric> RAID_HOME_BLOCK_ENTITY;

    private static Block registerBlock(String name, Block block, Function<Block, BlockItem> itemFactory) {
        registerBlockItem(name, itemFactory.apply(block));
        return Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            block
        );
    }

    private static void registerBlockItem(String name, BlockItem blockItem) {
        Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            blockItem
        );
    }

    public static void registerModBlocks() {
        RAID_CRYSTAL_BLOCK = registerBlock(
            "raid_crystal_block",
            new RaidCrystalBlockFabric(ModBlocks.PROPERTIES),
            block -> new RaidCrystalBlockItem(block, new Item.Properties())
        );
        RAID_HOME_BLOCK = registerBlock(
            "raid_home_block",
            new RaidHomeBlockFabric(ModBlocks.HOME_BLOCK_PROPERTIES),
            block -> new RaidHomeBlockItem(block, new Item.Properties())
        );

        RAID_CRYSTAL_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_crystal_block_entity"),
            BlockEntityType.Builder.of(RaidCrystalBlockEntityFabric::new, RAID_CRYSTAL_BLOCK).build()
        );
        RAID_HOME_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_home_block_entity"),
            BlockEntityType.Builder.of(RaidHomeBlockEntityFabric::new, RAID_HOME_BLOCK).build()
        );

        INSTANCE = new FabricBlocks();
    }

    @Override
    public Block getRaidCrystalBlock() {
        return RAID_CRYSTAL_BLOCK;
    }

    @Override
    public Block getRaidHomeBlock() {
        return RAID_HOME_BLOCK;
    }
}
