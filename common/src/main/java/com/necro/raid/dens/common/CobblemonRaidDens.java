package com.necro.raid.dens.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.config.*;
import com.necro.raid.dens.common.data.ScriptAdapter;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.statistics.RaidStatistics;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IShinyRate;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CobblemonRaidDens {
    public static final String MOD_ID = "cobblemonraiddens";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static RaidConfig CONFIG;
    public static BlacklistConfig BLACKLIST_CONFIG;
    public static final Map<RaidTier, TierConfig> TIER_CONFIG = new HashMap<>();

    public static void init() {
        LOGGER.info("Initialising {}", MOD_ID);

        AutoConfig.register(RaidConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(RaidConfig.class).getConfig();
        AutoConfig.register(BlacklistConfig.class, JanksonConfigSerializer::new);
        BLACKLIST_CONFIG = AutoConfig.getConfigHolder(BlacklistConfig.class).getConfig();

        Jankson jankson = Jankson.builder()
            .registerSerializer(ScriptAdapter.class, (script, marshaller) -> script.serialize())
            .registerDeserializer(JsonElement.class, ScriptAdapter.class, (json, marshaller) -> ScriptAdapter.deserialize(json))
            .build();

        AutoConfig.register(TierOneConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_ONE, AutoConfig.getConfigHolder(TierOneConfig.class).getConfig());
        AutoConfig.register(TierTwoConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_TWO, AutoConfig.getConfigHolder(TierTwoConfig.class).getConfig());
        AutoConfig.register(TierThreeConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_THREE, AutoConfig.getConfigHolder(TierThreeConfig.class).getConfig());
        AutoConfig.register(TierFourConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_FOUR, AutoConfig.getConfigHolder(TierFourConfig.class).getConfig());
        AutoConfig.register(TierFiveConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_FIVE, AutoConfig.getConfigHolder(TierFiveConfig.class).getConfig());
        AutoConfig.register(TierSixConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_SIX, AutoConfig.getConfigHolder(TierSixConfig.class).getConfig());
        AutoConfig.register(TierSevenConfig.class, (config, cls) -> new JanksonConfigSerializer<>(config, cls, jankson));
        TIER_CONFIG.put(RaidTier.TIER_SEVEN, AutoConfig.getConfigHolder(TierSevenConfig.class).getConfig());

        RaidUtils.init();
        RaidStatistics.init();
        RaidDenCriteriaTriggers.init();

        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, event -> {
            raidFailEvent(event.getBattle());
            return Unit.INSTANCE;
        });
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
            BattlePokemon battlePokemon = event.getLosers().getFirst().getActivePokemon().getFirst().getBattlePokemon();
            if (battlePokemon != null && battlePokemon.getEffectedPokemon().getOwnerPlayer() != null) return Unit.INSTANCE;
            raidFailEvent(event.getBattle());
            return Unit.INSTANCE;
        });
        CobblemonEvents.LOOT_DROPPED.subscribe(Priority.HIGHEST, event -> {
            cancelLootDrops(event);
            return Unit.INSTANCE;
        });
        CobblemonEvents.SHINY_CHANCE_CALCULATION.subscribe(Priority.HIGHEST, event -> {
            setRaidShinyRate(event);
            return Unit.INSTANCE;
        });

        RaidEvents.RAID_JOIN.subscribe(Priority.NORMAL, event -> {
            RaidDenNetworkMessages.JOIN_RAID.accept(event.getPlayer(), true);
            return Unit.INSTANCE;
        });
    }

    @SuppressWarnings("ConstantConditions")
    private static void raidFailEvent(PokemonBattle battle) {
        try {
            UUID battleId = ((IRaidAccessor) battle.getSide2().getActivePokemon().getFirst().getBattlePokemon().getEntity()).getRaidId();
            if (RaidHelper.ACTIVE_RAIDS.containsKey(battleId)) {
                RaidInstance raidInstance = RaidHelper.ACTIVE_RAIDS.get(battleId);
                raidInstance.removePlayer(battle);
            }
        }
        catch (NullPointerException ignored) {}
    }

    private static void cancelLootDrops(LootDroppedEvent event) {
        if (!(event.getEntity() instanceof PokemonEntity pokemonEntity)) return;
        else if (!((IRaidAccessor) pokemonEntity).isRaidBoss()) return;
        event.cancel();
    }

    private static void setRaidShinyRate(ShinyChanceCalculationEvent event) {
        event.addModificationFunction((chance, player, pokemon) -> {
            Float shinyRate = ((IShinyRate) pokemon).getRaidShinyRate();
            return shinyRate == null || shinyRate < 0 ? chance : shinyRate;
        });
    }
}
