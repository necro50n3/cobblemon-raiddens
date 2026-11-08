package com.necro.raid.dens.common.loot.predicate;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;

public class RaidLootPredicates {
    public static Holder<ItemSubPredicate.Type<RaidTierPredicate>> ITEM_TIER_PREDICATE;
    public static Holder<ItemSubPredicate.Type<RaidTypePredicate>> ITEM_TYPE_PREDICATE;
    public static Holder<ItemSubPredicate.Type<RaidFeaturePredicate>> ITEM_FEATURE_PREDICATE;
    public static Holder<ItemSubPredicate.Type<RaidAdventurePredicate>> ITEM_ADVENTURE_PREDICATE;
}
