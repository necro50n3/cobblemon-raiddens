package com.necro.raid.dens.fabric.loot;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.loot.condition.RaidPouchCondition;
import com.necro.raid.dens.common.loot.condition.RaidLootConditions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class FabricLootConditions {
    private static Holder<LootItemConditionType> register(String name, LootItemConditionType type) {
        return Holder.direct(Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name), type));
    }

    public static void registerLootConditions() {
        RaidLootConditions.RAID_POUCH_CONDITION = register("raid_pouch_condition", RaidPouchCondition.TYPE);
    }
}
