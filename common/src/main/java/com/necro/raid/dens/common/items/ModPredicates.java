package com.necro.raid.dens.common.items;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;

public class ModPredicates {
    public static Holder<ItemSubPredicate.Type<RaidTierPredicate>> ITEM_TIER_PREDICATE;
    public static Holder<ItemSubPredicate.Type<RaidTypePredicate>> ITEM_TYPE_PREDICATE;
}
