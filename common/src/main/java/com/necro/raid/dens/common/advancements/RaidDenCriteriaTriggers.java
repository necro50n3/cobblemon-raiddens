package com.necro.raid.dens.common.advancements;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.data.raid.RaidTier;
import kotlin.Unit;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

public class RaidDenCriteriaTriggers {
    public static Holder<JoinRaidDenTrigger> JOIN_RAID_DEN;
    public static Holder<RaidTierTrigger> RAID_TIER;
    public static Holder<RaidFeatureTrigger> RAID_FEATURE;
    public static Holder<RaidShinyTrigger> RAID_SHINY;
    public static Holder<RaidAdventureTrigger> RAID_ADVENTURE;

    public static void triggerJoinRaid(ServerPlayer player) {
        JOIN_RAID_DEN.value().trigger(player);
    }

    public static void triggerRaidTier(ServerPlayer player, RaidTier tier) {
        RAID_TIER.value().trigger(player, tier);
    }

    public static void triggerRaidFeature(ServerPlayer player, String feature) {
        RAID_FEATURE.value().trigger(player, feature);
    }

    public static void triggerRaidShiny(ServerPlayer player, Pokemon pokemon) {
        RAID_SHINY.value().trigger(player, pokemon.getShiny());
    }

    public static void triggerRaidAdventure(ServerPlayer player, boolean adventure) {
        RAID_ADVENTURE.value().trigger(player, adventure);
    }

    public static void init() {
        RaidEvents.RAID_JOIN.subscribe(Priority.LOWEST, event -> {
            triggerJoinRaid(event.getPlayer());
            return Unit.INSTANCE;
        });

        RaidEvents.RAID_BATTLE_END.subscribe(Priority.LOWEST, event -> {
            if (!event.isWin()) return Unit.INSTANCE;
            triggerRaidTier(event.player(), event.raidBoss().getTier());
            triggerRaidFeature(event.player(), event.raidBoss().getFeature());
            triggerRaidAdventure(event.player(), event.raidBoss().isAdventure());
            return Unit.INSTANCE;
        });
    }
}
