package com.necro.raid.dens.common.compat.emi;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@EmiEntrypoint
public class RaidDensEMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        ItemStack stack = ModItems.RAID_SHARD.value().getDefaultInstance();
        stack.set(ModComponents.RAID_ENERGY.value(), Integer.MAX_VALUE);

        registry.addRecipe(new EmiInfoRecipe(
            List.of(EmiStack.of(stack)),
            List.of(Component.translatable("xei.cobblemonraiddens.raid_shard")),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "/raid_shard")
        ));
    }
}
