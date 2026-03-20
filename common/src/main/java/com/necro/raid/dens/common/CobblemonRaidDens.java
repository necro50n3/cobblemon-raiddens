package com.necro.raid.dens.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.drops.LootDroppedEvent;
import com.cobblemon.mod.common.api.events.entity.SpawnEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSentEvent;
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.advancements.RaidDenCriteriaTriggers;
import com.necro.raid.dens.common.config.*;
import com.necro.raid.dens.common.data.raid.Script;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.raids.battle.RaidConditions;
import com.necro.raid.dens.common.raids.status.ShieldStatus;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.registry.RaidScriptRegistry;
import com.necro.raid.dens.common.statistics.RaidStatistics;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import com.necro.raid.dens.common.util.IShinyRate;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CobblemonRaidDens {
    public static final String MOD_ID = "cobblemonraiddens";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static RaidConfig CONFIG;
    public static BlacklistConfig BLACKLIST_CONFIG;
    public static ConditionsConfig CONDITIONS_CONFIG;
    public static final Map<RaidTier, TierConfig> TIER_CONFIG = new EnumMap<>(RaidTier.class);

    public static void init() {
        LOGGER.info("Initialising {}", MOD_ID);

        initConfig();
        initUtils();
        initRegistries();
        registerCobblemonEvents();
        RaidEvents.registerEvents();
    }

    private static void initConfig() {
        AutoConfig.register(RaidConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(RaidConfig.class).getConfig();
        AutoConfig.register(BlacklistConfig.class, JanksonConfigSerializer::new);
        BLACKLIST_CONFIG = AutoConfig.getConfigHolder(BlacklistConfig.class).getConfig();
        AutoConfig.register(ConditionsConfig.class, JanksonConfigSerializer::new);
        CONDITIONS_CONFIG = AutoConfig.getConfigHolder(ConditionsConfig.class).getConfig();

        Jankson jankson = Jankson.builder()
            .registerSerializer(Script.class, (script, marshaller) -> script.serialize())
            .registerDeserializer(JsonElement.class, Script.class, (json, marshaller) -> Script.deserialize(json))
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
    }

    private static void initUtils() {
        RaidUtils.init();
        RaidStatistics.init();
        RaidDenCriteriaTriggers.init();
        RaidConditions.init();
    }

    private static void initRegistries() {
        RaidScriptRegistry.init();
        CustomRaidRegistries.registerDefaults();
        Statuses.registerStatus(new ShieldStatus());
    }

    private static void registerCobblemonEvents() {
        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, event -> {
            raidFailEvent(event.getPlayer().getEntity(), event.getBattle(), true);
            return Unit.INSTANCE;
        });
        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
            BattlePokemon battlePokemon = event.getWinners().getFirst().getActivePokemon().getFirst().getBattlePokemon();
            if (battlePokemon == null || battlePokemon.getEntity() == null) return Unit.INSTANCE;
            else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return Unit.INSTANCE;
            raidFailEvent(null, event.getBattle(), false);
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
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGHEST, event -> {
            cancelWildSpawn(event);
            return Unit.INSTANCE;
        });
        CobblemonEvents.POKEMON_SENT_POST.subscribe(Priority.NORMAL, event -> {
            sendHealthBarPacket(event);
            return Unit.INSTANCE;
        });
    }

    private static void raidFailEvent(@Nullable ServerPlayer player, PokemonBattle battle, boolean ignoreLives) {
        try {
            RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
            if (raid == null || raid.isFinished()) return;
            if (player == null) raid.removePlayer(battle, ignoreLives);
            else raid.removePlayer(player, battle, ignoreLives);
        }
        catch (NullPointerException ignored) {}
    }

    private static void cancelLootDrops(LootDroppedEvent event) {
        if (!(event.getEntity() instanceof PokemonEntity pokemonEntity)) return;
        else if (!((IRaidAccessor) pokemonEntity).crd_isRaidBoss()) return;
        event.cancel();
    }

    private static void setRaidShinyRate(ShinyChanceCalculationEvent event) {
        event.addModificationFunction((chance, player, pokemon) -> {
            Float shinyRate = ((IShinyRate) pokemon).crd_getRaidShinyRate();
            return shinyRate == null || shinyRate < 0 ? chance : shinyRate;
        });
    }

    private static void cancelWildSpawn(SpawnEvent<PokemonEntity> event) {
        if (!RaidUtils.isRaidDimension(event.getSpawnablePosition().getWorld())) return;
        else if (event.getEntity().getOwner() != null) return;
        else if (((IRaidAccessor) event.getEntity()).crd_isRaidBoss()) return;
        event.cancel();
    }

    @SuppressWarnings("ConstantConditions")
    private static void sendHealthBarPacket(PokemonSentEvent.Post event) {
        PokemonEntity pokemonEntity = event.getPokemonEntity();
        if (pokemonEntity == null) return;
        PokemonBattle battle = pokemonEntity.getBattle();
        if (battle == null || !((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        raid.getPlayers().forEach(player -> RaidDenNetworkMessages.RAID_HEALTH_BAR.accept(player, List.of(pokemonEntity.getId()), true));
    }
}
