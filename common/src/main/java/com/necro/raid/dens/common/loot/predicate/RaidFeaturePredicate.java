package com.necro.raid.dens.common.loot.predicate;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidFeaturePredicate(RaidFeature feature) implements ItemSubPredicate {
    public static final Codec<RaidFeaturePredicate> CODEC;

    @Override
    public boolean matches(ItemStack itemStack) {
        RaidFeature feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        if (feature == null) return false;
        return this.feature == feature;
    }

    public static RaidFeaturePredicate ofFeature(RaidFeature feature) {
        return new RaidFeaturePredicate(feature);
    }

    static {
        CODEC = RaidFeature.codec().xmap(RaidFeaturePredicate::new, RaidFeaturePredicate::feature);
    }
}
