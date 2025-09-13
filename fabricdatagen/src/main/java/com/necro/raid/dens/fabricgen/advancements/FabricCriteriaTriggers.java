package com.necro.raid.dens.fabricgen.advancements;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.advancements.*;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FabricCriteriaTriggers {
    public static void registerCriteriaTriggers() {
        RaidDenCriteriaTriggers.JOIN_RAID_DEN = Holder.direct(
            Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "joined_raid"),
                new JoinRaidDenTrigger()
            )
        );

        RaidDenCriteriaTriggers.RAID_TIER = Holder.direct(
            Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "completed_tier"),
                new RaidTierTrigger()
            )
        );

        RaidDenCriteriaTriggers.RAID_FEATURE = Holder.direct(
            Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "completed_feature"),
                new RaidFeatureTrigger()
            )
        );

        RaidDenCriteriaTriggers.RAID_SHINY = Holder.direct(
            Registry.register(
                BuiltInRegistries.TRIGGER_TYPES,
                ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "shiny_from_raid"),
                new RaidShinyTrigger()
            )
        );
    }
}
