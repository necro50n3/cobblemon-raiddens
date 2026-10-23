package com.necro.raid.dens.common.loot.predicate;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record RaidAdventurePredicate(boolean isAdventure) implements ItemSubPredicate {
    public static final Codec<RaidAdventurePredicate> CODEC;

    @Override
    public boolean matches(ItemStack itemStack) {
        ResourceLocation adventure = itemStack.get(ModComponents.BOSS_COMPONENT.value());
        if (adventure == null) return false;
        else if (!CustomRaidRegistries.ADVENTURE_REGISTRY.containsKey(adventure)) return false;
        return CustomRaidRegistries.ADVENTURE_REGISTRY.get(adventure).isAdventure() == isAdventure;
    }

    public static RaidAdventurePredicate ofFlag(boolean isAdventure) {
        return new RaidAdventurePredicate(isAdventure);
    }

    static {
        CODEC = Codec.BOOL.xmap(RaidAdventurePredicate::new, RaidAdventurePredicate::isAdventure);
    }
}
