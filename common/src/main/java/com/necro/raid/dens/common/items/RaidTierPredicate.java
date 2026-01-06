package com.necro.raid.dens.common.items;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidTier;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidTierPredicate(RaidTier tier) implements ItemSubPredicate {
    public static final Codec<RaidTierPredicate> CODEC;

    public boolean matches(ItemStack itemStack) {
        RaidTier itemTier = itemStack.get(ModComponents.TIER_COMPONENT.value());
        if (itemTier == null) return false;
        return this.tier == itemTier;
    }

    public static RaidTierPredicate ofTier(RaidTier tier) {
        return new RaidTierPredicate(tier);
    }

    public RaidTier tier() {
        return this.tier;
    }

    static {
        CODEC = RaidTier.codec().xmap(RaidTierPredicate::new, RaidTierPredicate::tier);
    }
}
