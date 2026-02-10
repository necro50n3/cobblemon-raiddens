package com.necro.raid.dens.common.loot.predicate;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;

@SuppressWarnings("unused")
public class RaidLootPredicates {
    public static Holder<ItemSubPredicate.Type<RaidTierPredicate>> ITEM_TIER_PREDICATE;
    public static Holder<ItemSubPredicate.Type<RaidTypePredicate>> ITEM_TYPE_PREDICATE;
}
