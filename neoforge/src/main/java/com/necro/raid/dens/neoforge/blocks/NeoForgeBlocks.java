package com.necro.raid.dens.neoforge.blocks;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.block.RaidHomeBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import com.necro.raid.dens.common.items.item.RaidHomeBlockItem;
import com.necro.raid.dens.neoforge.items.NeoForgeItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CobblemonRaidDens.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerBlocks() {
        ModBlocks.RAID_CRYSTAL_BLOCK = register(
            "raid_crystal_block",
            () -> new RaidCrystalBlock(ModBlocks.PROPERTIES),
            block -> () -> new RaidCrystalBlockItem(block.get(), new Item.Properties())
        );
        ModBlocks.RAID_HOME_BLOCK = register(
            "raid_home_block",
            () -> new RaidHomeBlock(ModBlocks.HOME_BLOCK_PROPERTIES),
            block -> () -> new RaidHomeBlockItem(block.get(), new Item.Properties())
        );
        ModBlocks.RAIDITE_ORE = register(
            "raidite_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)),
            block -> () -> new BlockItem(block.get(), new Item.Properties())
        );
        ModBlocks.DEEPSLATE_RAIDITE_ORE = register(
            "deepslate_raidite_ore",
            () -> new DropExperienceBlock(UniformInt.of(3, 7), BlockBehaviour.Properties.ofFullCopy(ModBlocks.RAIDITE_ORE.value()).mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE)),
            block -> () -> new BlockItem(block.get(), new Item.Properties())
        );

        ModBlocks.RAID_CRYSTAL_BLOCK_ENTITY = register(
            "raid_crystal_block_entity",
            (blockPos, blockState) -> new RaidCrystalBlockEntity(ModBlocks.RAID_CRYSTAL_BLOCK_ENTITY.value(), blockPos, blockState),
            ModBlocks.RAID_CRYSTAL_BLOCK
        );
        ModBlocks.RAID_HOME_BLOCK_ENTITY = register(
            "raid_home_block_entity",
            (blockPos, blockState) -> new RaidHomeBlockEntity(ModBlocks.RAID_HOME_BLOCK_ENTITY.value(), blockPos, blockState),
            ModBlocks.RAID_HOME_BLOCK
        );
    }

    @SuppressWarnings("unchecked")
    private static <T extends Block> Holder<T> register(String name, Supplier<T> block, Function<DeferredBlock<T>, Supplier<BlockItem>> itemFactory) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, itemFactory.apply(toReturn));
        return (Holder<T>) toReturn;
    }

    private static void registerBlockItem(String name, Supplier<BlockItem> blockItem) {
        NeoForgeItems.ITEMS.register(name, blockItem);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> Holder<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, Holder<? extends Block> block) {
        return (Holder<BlockEntityType<T>>) (Object) BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(blockEntity, block.value()).build(null));
    }
}
