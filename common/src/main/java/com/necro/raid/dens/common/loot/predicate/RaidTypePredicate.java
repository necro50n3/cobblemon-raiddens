package com.necro.raid.dens.common.loot.predicate;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public record RaidTypePredicate(RaidType type) implements ItemSubPredicate {
    public static final Codec<RaidTypePredicate> CODEC;

    public boolean matches(ItemStack itemStack) {
        RaidType type = itemStack.get(ModComponents.TYPE_COMPONENT.value());
        if (type == null) return false;
        return this.type == type;
    }

    public static RaidTypePredicate ofType(RaidType raidType) {
        return new RaidTypePredicate(raidType);
    }

    static {
        CODEC = RaidType.codec().xmap(RaidTypePredicate::new, RaidTypePredicate::type);
    }
}
