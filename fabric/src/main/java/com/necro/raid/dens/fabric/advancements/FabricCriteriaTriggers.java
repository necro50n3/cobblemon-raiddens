package com.necro.raid.dens.fabric.advancements;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.advancements.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricCriteriaTriggers {
    public static void registerCriteriaTriggers() {
        RaidDenCriteriaTriggers.JOIN_RAID_DEN = register("joined_raid", new JoinRaidDenTrigger());
        RaidDenCriteriaTriggers.RAID_TIER = register("completed_tier", new RaidTierTrigger());
        RaidDenCriteriaTriggers.RAID_FEATURE = register("completed_feature", new RaidFeatureTrigger());
        RaidDenCriteriaTriggers.RAID_SHINY = register("shiny_from_raid", new RaidShinyTrigger());
        RaidDenCriteriaTriggers.RAID_ADVENTURE = register("completed_adventure", new RaidAdventureTrigger());
    }

    private static <T extends CriterionTrigger<? extends CriterionTriggerInstance>> Holder<T> register(String name, T trigger) {
        return Holder.direct(Registry.register(BuiltInRegistries.TRIGGER_TYPES, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name), trigger));
    }
}
