package com.necro.raid.dens.common.raids.helpers;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.necro.raid.dens.common.showdown.events.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RaidScriptHelper {
    private static final Map<String, Consumer<PokemonBattle>> STATIC_SCRIPTS = new HashMap<>();

    public static Consumer<PokemonBattle> decode(String function) {
        try {
            return decodeInner(function);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Consumer<PokemonBattle> decodeInner(String function) {
        Consumer<PokemonBattle> script = STATIC_SCRIPTS.get(function);
        if (script != null) return script;

        if (function.startsWith("BOSS")) {
            String[] args = function.split("_");
            if (args.length != 3) return null;

            Stat stat = parseStat(args[1]);
            if (stat == null) return null;
            int stages = Math.clamp(parseInt(args[2]), -6, 6);
            return battle -> new StatBoostShowdownEvent(stat, stages, 2, false).send(battle);
        }
        else if (function.startsWith("PLAYER")) {
            String[] args = function.split("_");
            if (args.length != 3) return null;

            Stat stat = parseStat(args[1]);
            if (stat == null) return null;
            int stages = Math.clamp(parseInt(args[2]), -6, 6);
            return battle -> new StatBoostShowdownEvent(stat, -stages, 1, false).send(battle);
        }
        else if (function.startsWith("USE_MOVE")) {
            String[] args = function.split("_");
            if (args.length != 4) return null;

            String move = args[2].toLowerCase();
            int target = parseInt(args[3]);
            return battle -> new UseMoveShowdownEvent(move, target).send(battle);
        }

        return null;
    }

    private static Stat parseStat(String stat) {
        return switch (stat) {
            case "ATK" -> Stats.ATTACK;
            case "DEF" -> Stats.DEFENCE;
            case "SPA" -> Stats.SPECIAL_ATTACK;
            case "SPD" -> Stats.SPECIAL_DEFENCE;
            case "SPE" -> Stats.SPEED;
            case "ACC" -> Stats.ACCURACY;
            case "EVA" -> Stats.EVASION;
            default -> null;
        };
    }

    private static int parseInt(String number) throws NumberFormatException {
        return Integer.parseInt(number);
    }

    static {
        STATIC_SCRIPTS.put("RESET_BOSS", battle -> new ResetBossShowdownEvent().send(battle));
        STATIC_SCRIPTS.put("RESET_PLAYER", battle -> new ResetPlayerShowdownEvent().send(battle));

        STATIC_SCRIPTS.put("SET_RAIN", battle -> new SetWeatherShowdownEvent("raindance", false).send(battle));
        STATIC_SCRIPTS.put("SET_SANDSTORM", battle -> new SetWeatherShowdownEvent("sandstorm", false).send(battle));
        STATIC_SCRIPTS.put("SET_SNOW", battle -> new SetWeatherShowdownEvent("snow", false).send(battle));
        STATIC_SCRIPTS.put("SET_SUN", battle -> new SetWeatherShowdownEvent("sunnyday", false).send(battle));

        STATIC_SCRIPTS.put("SET_ELECTRIC_TERRAIN", battle -> new SetTerrainShowdownEvent("electricterrain", false).send(battle));
        STATIC_SCRIPTS.put("SET_GRASSY_TERRAIN", battle -> new SetTerrainShowdownEvent("grassyterrain", false).send(battle));
        STATIC_SCRIPTS.put("SET_MISTY_TERRAIN", battle -> new SetTerrainShowdownEvent("mistyterrain", false).send(battle));
        STATIC_SCRIPTS.put("SET_PSYCHIC_TERRAIN", battle -> new SetTerrainShowdownEvent("psychicterrain", false).send(battle));

        STATIC_SCRIPTS.put("SHIELD_UP", battle -> new ShieldAddShowdownEvent().send(battle));
        STATIC_SCRIPTS.put("SHIELD_DOWN", battle -> new ShieldRemoveShowdownEvent().send(battle));
    }
}
