package com.necro.raid.dens.neoforge.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.loot.predicate.*;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgePredicates {
    public static final DeferredRegister<ItemSubPredicate.Type<?>> PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerPredicates() {
        RaidLootPredicates.ITEM_TIER_PREDICATE = register("raid_tier", () -> new ItemSubPredicate.Type<>(RaidTierPredicate.CODEC));
        RaidLootPredicates.ITEM_TYPE_PREDICATE = register("raid_type", () -> new ItemSubPredicate.Type<>(RaidTypePredicate.CODEC));
        RaidLootPredicates.ITEM_FEATURE_PREDICATE = register("raid_feature", () -> new ItemSubPredicate.Type<>(RaidFeaturePredicate.CODEC));
        RaidLootPredicates.ITEM_ADVENTURE_PREDICATE = register("raid_adventure", () -> new ItemSubPredicate.Type<>(RaidAdventurePredicate.CODEC));
    }

    @SuppressWarnings("unchecked")
    private static <T extends ItemSubPredicate> Holder<ItemSubPredicate.Type<T>> register(String name, Supplier<ItemSubPredicate.Type<T>> type) {
        return (Holder<ItemSubPredicate.Type<T>>) (Object) PREDICATES.register(name, type);
    }
}
