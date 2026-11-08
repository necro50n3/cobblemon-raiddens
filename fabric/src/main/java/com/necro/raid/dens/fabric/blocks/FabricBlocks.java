package com.necro.raid.dens.fabric.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.block.RaidHomeBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import com.necro.raid.dens.common.items.item.RaidHomeBlockItem;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Function;

public class FabricBlocks {
    public static void registerBlocks() {
        ModBlocks.RAID_CRYSTAL_BLOCK = register(
            "raid_crystal_block",
            new RaidCrystalBlock(ModBlocks.PROPERTIES),
            block -> new RaidCrystalBlockItem(block, new Item.Properties())
        );
        ModBlocks.RAID_HOME_BLOCK = register(
            "raid_home_block",
            new RaidHomeBlock(ModBlocks.HOME_BLOCK_PROPERTIES),
            block -> new RaidHomeBlockItem(block, new Item.Properties())
        );
        ModBlocks.RAIDITE_ORE = register(
            "raidite_ore",
            new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)),
            block -> new BlockItem(block, new Item.Properties())
        );
        ModBlocks.DEEPSLATE_RAIDITE_ORE = register(
            "deepslate_raidite_ore",
            new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofFullCopy(ModBlocks.RAIDITE_ORE.value()).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)),
            block -> new BlockItem(block, new Item.Properties())
        );

        ModBlocks.RAID_CRYSTAL_BLOCK_ENTITY = register(
            "raid_crystal_block_entity",
            ((blockPos, blockState) -> new RaidCrystalBlockEntity(ModBlocks.RAID_CRYSTAL_BLOCK_ENTITY.value(), blockPos, blockState)),
            ModBlocks.RAID_CRYSTAL_BLOCK.value()
        );
        ModBlocks.RAID_HOME_BLOCK_ENTITY = register(
            "raid_home_block_entity",
            ((blockPos, blockState) -> new RaidHomeBlockEntity(ModBlocks.RAID_HOME_BLOCK_ENTITY.value(), blockPos, blockState)),
            ModBlocks.RAID_HOME_BLOCK.value()
        );
    }

    private static <T extends Block> Holder<T> register(String name, T block, Function<T, BlockItem> itemFactory) {
        registerBlockItem(name, itemFactory.apply(block));
        return Holder.direct(Registry.register(
            BuiltInRegistries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            block
        ));
    }

    private static void registerBlockItem(String name, BlockItem blockItem) {
        Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            blockItem
        );
    }

    private static <T extends BlockEntity> Holder<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, Block block) {
        return Holder.direct(Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name),
            BlockEntityType.Builder.of(blockEntity, block).build()
        ));
    }
}
