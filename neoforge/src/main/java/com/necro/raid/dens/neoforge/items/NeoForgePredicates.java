package com.necro.raid.dens.neoforge.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.ModPredicates;
import com.necro.raid.dens.common.items.RaidTierPredicate;
import com.necro.raid.dens.common.items.RaidTypePredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgePredicates {
    public static final DeferredRegister<ItemSubPredicate.Type<?>> PREDICATES = DeferredRegister.create(Registries.ITEM_SUB_PREDICATE_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerPredicates() {
        ModPredicates.ITEM_TIER_PREDICATE = (Holder<ItemSubPredicate.Type<RaidTierPredicate>>) (Object)  PREDICATES.register("raid_tier", () -> new ItemSubPredicate.Type<>(RaidTierPredicate.CODEC));
        ModPredicates.ITEM_TYPE_PREDICATE = (Holder<ItemSubPredicate.Type<RaidTypePredicate>>) (Object)  PREDICATES.register("raid_type", () -> new ItemSubPredicate.Type<>(RaidTypePredicate.CODEC));
    }
}
