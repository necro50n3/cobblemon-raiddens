package com.necro.raid.dens.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.necro.raid.dens.common.config.ClientConfig;
import com.necro.raid.dens.common.config.RaidConfig;
import com.necro.raid.dens.common.loot.LootFunctions;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import kotlin.Unit;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CobblemonRaidDens {
    public static final String MOD_ID = "cobblemonraiddens";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static RaidConfig CONFIG;
    public static ClientConfig CLIENT_CONFIG;

    public static void init() {
        LOGGER.info("Initialising {}", MOD_ID);

        AutoConfig.register(RaidConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(RaidConfig.class).getConfig();
        AutoConfig.register(ClientConfig.class, JanksonConfigSerializer::new);
        CLIENT_CONFIG = AutoConfig.getConfigHolder(ClientConfig.class).getConfig();

        CobblemonEvents.BATTLE_FLED.subscribe(Priority.NORMAL, event -> {
            raidFailEvent(event.getBattle());
            return Unit.INSTANCE;
        });
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, event -> {
            raidFailEvent(event.getBattle());
            return Unit.INSTANCE;
        });
    }

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
}
