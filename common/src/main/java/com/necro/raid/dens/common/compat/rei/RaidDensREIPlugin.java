package com.necro.raid.dens.common.compat.rei;

import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class RaidDensREIPlugin implements REIClientPlugin {
    @Override
    public void registerDisplays(DisplayRegistry registry) {
        ItemStack stack = ModItems.RAID_SHARD.value().getDefaultInstance();
        stack.set(ModComponents.RAID_ENERGY.value(), Integer.MAX_VALUE);

        registry.add(DefaultInformationDisplay.createFromEntry(
            EntryStacks.of(stack),
            Component.translatable("item.cobblemonraiddens.raid_shard")
        ).line(Component.translatable("xei.cobblemonraiddens.raid_shard")));
    }
}
