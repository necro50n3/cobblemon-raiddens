package com.necro.raid.dens.neoforge.statistics;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.statistics.RaidStatistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeStatistics {
    public static final DeferredRegister<ResourceLocation> CUSTOM_STATS = DeferredRegister.create(Registries.CUSTOM_STAT, CobblemonRaidDens.MOD_ID);

    public static void registerStatistics() {
        CUSTOM_STATS.register(RaidStatistics.RAIDS_HOSTED.getPath(), () -> RaidStatistics.RAIDS_HOSTED);
        CUSTOM_STATS.register(RaidStatistics.RAIDS_JOINED.getPath(), () -> RaidStatistics.RAIDS_JOINED);
        CUSTOM_STATS.register(RaidStatistics.RAIDS_COMPLETED.getPath(), () -> RaidStatistics.RAIDS_COMPLETED);

        CUSTOM_STATS.register(RaidStatistics.TIER_ONE_COMPLETED.getPath(), () -> RaidStatistics.TIER_ONE_COMPLETED);
        CUSTOM_STATS.register(RaidStatistics.TIER_TWO_COMPLETED.getPath(), () -> RaidStatistics.TIER_TWO_COMPLETED);
        CUSTOM_STATS.register(RaidStatistics.TIER_THREE_COMPLETED.getPath(), () -> RaidStatistics.TIER_THREE_COMPLETED);
        CUSTOM_STATS.register(RaidStatistics.TIER_FOUR_COMPLETED.getPath(), () -> RaidStatistics.TIER_FOUR_COMPLETED);
        CUSTOM_STATS.register(RaidStatistics.TIER_FIVE_COMPLETED.getPath(), () -> RaidStatistics.TIER_FIVE_COMPLETED);
        CUSTOM_STATS.register(RaidStatistics.TIER_SIX_COMPLETED.getPath(), () -> RaidStatistics.TIER_SIX_COMPLETED);
        CUSTOM_STATS.register(RaidStatistics.TIER_SEVEN_COMPLETED.getPath(), () -> RaidStatistics.TIER_SEVEN_COMPLETED);
    }
}
