package com.necro.raid.dens.common.loot.predicate;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidFeaturePredicate(String feature) implements ItemSubPredicate {
    public static final Codec<RaidFeaturePredicate> CODEC;

    @Override
    public boolean matches(ItemStack itemStack) {
        String feature = itemStack.get(ModComponents.FEATURE_COMPONENT.value());
        if (feature == null) return false;
        return this.feature.equalsIgnoreCase(feature);
    }

    public static RaidFeaturePredicate ofFeature(String feature) {
        return new RaidFeaturePredicate(feature);
    }

    static {
        CODEC = Codec.STRING.xmap(RaidFeaturePredicate::new, RaidFeaturePredicate::feature);
    }
}
