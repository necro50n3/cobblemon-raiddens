package com.necro.raid.dens.common.compat.jei;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class RaidDensJEIPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStack itemStack = ModItems.RAID_SHARD.value().getDefaultInstance();
        itemStack.set(ModComponents.RAID_ENERGY.value(), Integer.MAX_VALUE);
        registration.addIngredientInfo(List.of(itemStack), VanillaTypes.ITEM_STACK, Component.translatable("xei.cobblemonraiddens.raid_shard"));
    }
}
