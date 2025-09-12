package com.necro.raid.dens.common.statistics;

import com.cobblemon.mod.common.api.Priority;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.events.RaidEvents;
import kotlin.Unit;
import net.minecraft.resources.ResourceLocation;

public class RaidStatistics {
    public static final ResourceLocation RAIDS_HOSTED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raids_hosted");
    public static final ResourceLocation RAIDS_JOINED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raids_joined");
    public static final ResourceLocation RAIDS_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raids_completed");

    public static final ResourceLocation TIER_ONE_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_one_completed");
    public static final ResourceLocation TIER_TWO_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_two_completed");
    public static final ResourceLocation TIER_THREE_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_three_completed");
    public static final ResourceLocation TIER_FOUR_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_four_completed");
    public static final ResourceLocation TIER_FIVE_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_five_completed");
    public static final ResourceLocation TIER_SIX_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_six_completed");
    public static final ResourceLocation TIER_SEVEN_COMPLETED = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "tier_seven_completed");

    public static void init() {
        RaidEvents.RAID_JOIN.subscribe(Priority.LOWEST, event -> {
            if (event.isHost()) event.getPlayer().awardStat(RAIDS_HOSTED);
            event.getPlayer().awardStat(RAIDS_JOINED);
            return Unit.INSTANCE;
        });

        RaidEvents.RAID_END.subscribe(Priority.LOWEST, event -> {
            if (!event.isWin()) return Unit.INSTANCE;
            event.getPlayer().awardStat(RAIDS_COMPLETED);

            ResourceLocation tier = switch (event.getRaidBoss().getTier()) {
                case TIER_ONE -> TIER_ONE_COMPLETED;
                case TIER_TWO -> TIER_TWO_COMPLETED;
                case TIER_THREE -> TIER_THREE_COMPLETED;
                case TIER_FOUR -> TIER_FOUR_COMPLETED;
                case TIER_FIVE -> TIER_FIVE_COMPLETED;
                case TIER_SIX -> TIER_SIX_COMPLETED;
                case TIER_SEVEN -> TIER_SEVEN_COMPLETED;
            };
            event.getPlayer().awardStat(tier);
            return Unit.INSTANCE;
        });
    }
}
