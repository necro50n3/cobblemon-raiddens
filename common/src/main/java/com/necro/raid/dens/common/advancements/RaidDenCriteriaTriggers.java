package com.necro.raid.dens.common.advancements;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import kotlin.Unit;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

public class RaidDenCriteriaTriggers {
    public static Holder<? extends CriterionTrigger<? extends CriterionTriggerInstance>> JOIN_RAID_DEN;
    public static Holder<? extends CriterionTrigger<? extends CriterionTriggerInstance>> RAID_TIER;
    public static Holder<? extends CriterionTrigger<? extends CriterionTriggerInstance>> RAID_FEATURE;
    public static Holder<? extends CriterionTrigger<? extends CriterionTriggerInstance>> RAID_SHINY;

    public static void triggerJoinRaid(ServerPlayer player) {
        ((JoinRaidDenTrigger) JOIN_RAID_DEN.value()).trigger(player);
    }

    public static void triggerRaidTier(ServerPlayer player, RaidTier tier) {
        ((RaidTierTrigger) RAID_TIER.value()).trigger(player, tier);
    }

    public static void triggerRaidFeature(ServerPlayer player, RaidFeature feature) {
        ((RaidFeatureTrigger) RAID_FEATURE.value()).trigger(player, feature);
    }

    public static void triggerRaidShiny(ServerPlayer player, Pokemon pokemon) {
        ((RaidShinyTrigger) RAID_SHINY.value()).trigger(player, pokemon.getShiny());
    }

    public static void init() {
        RaidEvents.RAID_JOIN.subscribe(Priority.LOWEST, event -> {
            triggerJoinRaid(event.getPlayer());
            return Unit.INSTANCE;
        });

        RaidEvents.RAID_END.subscribe(Priority.LOWEST, event -> {
            triggerRaidTier(event.getPlayer(), event.getRaidBoss().getTier());
            triggerRaidFeature(event.getPlayer(), event.getRaidBoss().getFeature());
            return Unit.INSTANCE;
        });
    }
}
