package com.necro.raid.dens.common.loot.predicate;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidAdventurePredicate(boolean isAdventure) implements ItemSubPredicate {
    public static final Codec<RaidAdventurePredicate> CODEC;

    @Override
    public boolean matches(ItemStack itemStack) {
        boolean isAdventure = Boolean.TRUE.equals(itemStack.get(ModComponents.ADVENTURE_COMPONENT.value()));
        return isAdventure == this.isAdventure;
    }

    public static RaidAdventurePredicate ofFlag(boolean isAdventure) {
        return new RaidAdventurePredicate(isAdventure);
    }

    static {
        CODEC = Codec.BOOL.xmap(RaidAdventurePredicate::new, RaidAdventurePredicate::isAdventure);
    }
}
