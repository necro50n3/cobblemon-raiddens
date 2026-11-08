package com.necro.raid.dens.fabric.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.items.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class RaidDenTab {
    @SuppressWarnings("unused")
    public static final CreativeModeTab RAID_DEN_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_tab"),
        FabricItemGroup.builder().title(Component.translatable("itemgroup.cobblemonraiddens.raid_den_tab"))
            .icon(() -> new ItemStack(ModBlocks.RAID_CRYSTAL_BLOCK.value()))
            .displayItems((context, entries) -> {
                entries.accept(ModBlocks.RAID_CRYSTAL_BLOCK.value());
                entries.accept(ModItems.RAID_SHARD.value());
                entries.accept(ModItems.RAID_POUCH.value());
                entries.accept(ModItems.ATTACK_CHEER.value());
                entries.accept(ModItems.DEFENSE_CHEER.value());
                entries.accept(ModItems.HEAL_CHEER.value());
                entries.accept(ModItems.CATCHING_CHARM.value());
                entries.accept(ModBlocks.RAIDITE_ORE.value());
                entries.accept(ModBlocks.DEEPSLATE_RAIDITE_ORE.value());
                entries.accept(ModItems.RAIDITE.value());
            }).build()
    );

    public static void registerItemGroups() {}
}
