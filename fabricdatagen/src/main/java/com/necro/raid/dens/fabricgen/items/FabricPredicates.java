package com.necro.raid.dens.fabricgen.items;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.RaidTierPredicate;
import com.necro.raid.dens.common.items.ModPredicates;
import com.necro.raid.dens.common.items.RaidTypePredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricPredicates {
    public static void registerPredicates() {
        ModPredicates.ITEM_TIER_PREDICATE = Holder.direct(Registry.register(
            BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_tier"),
            new ItemSubPredicate.Type<>(RaidTierPredicate.CODEC)
        ));

        ModPredicates.ITEM_TYPE_PREDICATE = Holder.direct(Registry.register(
            BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_type"),
            new ItemSubPredicate.Type<>(RaidTypePredicate.CODEC)
        ));
    }
}
