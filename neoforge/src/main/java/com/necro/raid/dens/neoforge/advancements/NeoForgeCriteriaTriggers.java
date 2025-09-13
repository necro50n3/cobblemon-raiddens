package com.necro.raid.dens.neoforge.advancements;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.advancements.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, CobblemonRaidDens.MOD_ID);

    public static void registerCriteriaTriggers() {
        RaidDenCriteriaTriggers.JOIN_RAID_DEN = TRIGGERS.register("joined_raid", JoinRaidDenTrigger::new);
        RaidDenCriteriaTriggers.RAID_TIER = TRIGGERS.register("completed_tier", RaidTierTrigger::new);
        RaidDenCriteriaTriggers.RAID_FEATURE = TRIGGERS.register("completed_feature", RaidFeatureTrigger::new);
        RaidDenCriteriaTriggers.RAID_SHINY = TRIGGERS.register("shiny_from_raid", RaidShinyTrigger::new);
    }
}
