package com.necro.raid.dens.fabric.statistics;

import com.necro.raid.dens.common.statistics.RaidStatistics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class FabricStatistics {
    public static void registerStatistics() {
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.RAIDS_HOSTED, RaidStatistics.RAIDS_HOSTED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.RAIDS_JOINED, RaidStatistics.RAIDS_JOINED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.RAIDS_COMPLETED, RaidStatistics.RAIDS_COMPLETED);

        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_ONE_COMPLETED, RaidStatistics.TIER_ONE_COMPLETED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_TWO_COMPLETED, RaidStatistics.TIER_TWO_COMPLETED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_THREE_COMPLETED, RaidStatistics.TIER_THREE_COMPLETED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_FOUR_COMPLETED, RaidStatistics.TIER_FOUR_COMPLETED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_FIVE_COMPLETED, RaidStatistics.TIER_FIVE_COMPLETED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_SIX_COMPLETED, RaidStatistics.TIER_SIX_COMPLETED);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, RaidStatistics.TIER_SEVEN_COMPLETED, RaidStatistics.TIER_SEVEN_COMPLETED);
    }
}
