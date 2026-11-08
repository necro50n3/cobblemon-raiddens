package com.necro.raid.dens.neoforge.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RaidDenTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
        .create(Registries.CREATIVE_MODE_TAB, CobblemonRaidDens.MOD_ID);

    @SuppressWarnings("unused")
    public static final Supplier<CreativeModeTab> RAID_DEN_TAB = CREATIVE_TABS.register("raid_den_tab",
        () -> CreativeModeTab.builder().title(Component.translatable("itemgroup.cobblemonraiddens.raid_den_tab"))
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
            }).build());
}
