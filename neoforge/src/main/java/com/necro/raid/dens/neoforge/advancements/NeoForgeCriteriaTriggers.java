package com.necro.raid.dens.neoforge.advancements;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.advancements.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerCriteriaTriggers() {
        RaidDenCriteriaTriggers.JOIN_RAID_DEN = register("joined_raid", JoinRaidDenTrigger::new);
        RaidDenCriteriaTriggers.RAID_TIER = register("completed_tier", RaidTierTrigger::new);
        RaidDenCriteriaTriggers.RAID_FEATURE = register("completed_feature", RaidFeatureTrigger::new);
        RaidDenCriteriaTriggers.RAID_SHINY = register("shiny_from_raid", RaidShinyTrigger::new);
        RaidDenCriteriaTriggers.RAID_ADVENTURE = register("completed_adventure", RaidAdventureTrigger::new);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CriterionTrigger<? extends CriterionTriggerInstance>> Holder<T> register(String name, Supplier<T> trigger) {
        return (Holder<T>) TRIGGERS.register(name, trigger);
    }
}
