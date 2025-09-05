package com.necro.raid.dens.common.items;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidTypePredicate(RaidType raidType) implements ItemSubPredicate {
    public static final Codec<RaidTypePredicate> CODEC;

    public boolean matches(ItemStack itemStack) {
        RaidType itemType = itemStack.get(ModComponents.TYPE_COMPONENT.value());
        if (itemType == null) return false;
        return this.raidType == itemType;
    }

    public static RaidTypePredicate ofType(RaidType raidType) {
        return new RaidTypePredicate(raidType);
    }

    public RaidType raidType() {
        return this.raidType;
    }

    static {
        CODEC = RaidType.codec().xmap(RaidTypePredicate::new, RaidTypePredicate::raidType);
    }
}
