package com.necro.raid.dens.neoforge.loot;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.loot.condition.RaidPouchCondition;
import com.necro.raid.dens.common.loot.condition.RaidLootConditions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeLootConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
         DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, CobblemonRaidDens.MOD_ID);

    private static Holder<LootItemConditionType> register(String name, Supplier<LootItemConditionType> supplier) {
        return LOOT_CONDITION_TYPES.register(name, supplier);
    }

    public static void registerLootConditions() {
        RaidLootConditions.RAID_POUCH_CONDITION = register("raid_pouch_condition", () -> RaidPouchCondition.TYPE);
    }
}
