package com.necro.raid.dens.common.raids.helpers;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.necro.raid.dens.common.showdown.events.*;

import java.util.HashMap;
import java.util.Map;

public class RaidScriptHelper {
    private static final Map<String, ShowdownEvent> STATIC_SCRIPTS = new HashMap<>();

    public static ShowdownEvent decode(String function) {
        try {
            return decodeInner(function);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static ShowdownEvent decodeInner(String function) {
        ShowdownEvent script = STATIC_SCRIPTS.get(function);
        if (script != null) return script;

        if (function.startsWith("BOSS")) {
            String[] args = function.split("_");
            if (args.length != 3) return null;

            Stat stat = parseStat(args[1]);
            if (stat == null) return null;
            int stages = Math.clamp(parseInt(args[2]), -6, 6);
            return new RaidBoostShowdownEvent(stat, stages);
        }
        else if (function.startsWith("PLAYER")) {
            String[] args = function.split("_");
            if (args.length != 3) return null;

            Stat stat = parseStat(args[1]);
            if (stat == null) return null;
            int stages = Math.clamp(parseInt(args[2]), -6, 6);
            return new PlayerBoostShowdownEvent(stat, -stages);
        }
        else if (function.startsWith("USE_MOVE")) {
            String[] args = function.split("_");
            if (args.length != 4) return null;

            String move = args[2].toLowerCase();
            int target = parseInt(args[3]);
            return new UseMoveShowdownEvent(move, target);
        }
        else if (function.startsWith("TIMER")) {
            String[] args = function.split("_");
            if (args.length != 2) return null;

            int time = parseInt(args[1]);
            return new TimerRaidEvent(time);
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
        STATIC_SCRIPTS.put("RESET_BOSS", new ResetBossShowdownEvent());
        STATIC_SCRIPTS.put("RESET_PLAYER", new ResetPlayerShowdownEvent());

        STATIC_SCRIPTS.put("SET_RAIN", new SetWeatherShowdownEvent("raindance", false));
        STATIC_SCRIPTS.put("SET_SANDSTORM", new SetWeatherShowdownEvent("sandstorm", false));
        STATIC_SCRIPTS.put("SET_SNOW", new SetWeatherShowdownEvent("snow", false));
        STATIC_SCRIPTS.put("SET_SUN", new SetWeatherShowdownEvent("sunnyday", false));

        STATIC_SCRIPTS.put("SET_ELECTRIC_TERRAIN", new SetTerrainShowdownEvent("electricterrain", false));
        STATIC_SCRIPTS.put("SET_GRASSY_TERRAIN", new SetTerrainShowdownEvent("grassyterrain", false));
        STATIC_SCRIPTS.put("SET_MISTY_TERRAIN", new SetTerrainShowdownEvent("mistyterrain", false));
        STATIC_SCRIPTS.put("SET_PSYCHIC_TERRAIN", new SetTerrainShowdownEvent("psychicterrain", false));

        STATIC_SCRIPTS.put("SHIELD_UP", new ShieldAddShowdownEvent());
        STATIC_SCRIPTS.put("SHIELD_DOWN", new ShieldRemoveShowdownEvent());
    }
}
