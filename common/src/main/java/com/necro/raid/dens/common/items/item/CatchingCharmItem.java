package com.necro.raid.dens.common.items.item;

import com.necro.raid.dens.common.components.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CatchingCharmItem extends Item {
    public CatchingCharmItem() {
        super(new Item.Properties().rarity(Rarity.EPIC).component(ModComponents.CATCH_BOOST.value(), 0F));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cobblemonraiddens.cheer.tooltip").withStyle(ChatFormatting.GRAY));
        float boost = itemStack.getOrDefault(ModComponents.CATCH_BOOST.value(), 0F);
        tooltip.add(Component.translatable("item.cobblemonraiddens.catching_charm.tooltip.1", (Math.round(boost * 100))).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.cobblemonraiddens.catching_charm.tooltip.2", (Math.round(boost * 100))).withStyle(ChatFormatting.GRAY));
    }
}
