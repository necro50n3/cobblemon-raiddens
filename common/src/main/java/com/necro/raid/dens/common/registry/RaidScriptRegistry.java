package com.necro.raid.dens.common.registry;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.necro.raid.dens.common.raids.scripts.ScriptDecoder;
import com.necro.raid.dens.common.showdown.events.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RaidScriptRegistry {
    private static final Map<String, ScriptDecoder> SCRIPTS = new HashMap<>();
    private static final Map<String, Stat> STAT_MAP = Map.of(
        "ATK", Stats.ATTACK,
        "DEF", Stats.DEFENCE,
        "SPA", Stats.SPECIAL_ATTACK,
        "SPD", Stats.SPECIAL_DEFENCE,
        "SPE", Stats.SPEED,
        "ACC", Stats.ACCURACY,
        "EVA", Stats.EVASION
    );

    public static void registerDecoder(String key, ScriptDecoder event) {
        SCRIPTS.put(key, event);
    }

    public static void registerStatic(String key, AbstractEvent event) {
        SCRIPTS.put(key, args -> event);
    }

    public static AbstractEvent decode(String function) {
        try {
            return decodeInner(function);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static AbstractEvent decodeInner(String function) {
        if (function == null || function.isEmpty()) return null;
        String[] args = function.split("_");

        ScriptDecoder decoder = null;
        String key;
        for (int i = args.length; i > 0; i--) {
            key = String.join("_", Arrays.copyOfRange(args, 0, i));
            decoder = SCRIPTS.get(key);
            if (decoder != null) break;
        }

        return decoder == null ? null : decoder.decode(args);
    }

    private static int parseInt(String number) throws NumberFormatException {
        return Integer.parseInt(number);
    }

    private static double parseDouble(String number) throws NumberFormatException {
        return Double.parseDouble(number);
    }

    public static void init() {
        registerDecoder("BOSS", args -> {
            if (args.length != 3) return null;
            Stat stat = STAT_MAP.get(args[1]);
            if (stat == null) return null;
            int stages = Math.clamp(parseInt(args[2]), -6, 6);
            return new RaidBoostShowdownEvent(stat, stages);
        });
        registerDecoder("PLAYER", args -> {
            if (args.length != 3) return null;
            Stat stat = STAT_MAP.get(args[1]);
            if (stat == null) return null;
            int stages = Math.clamp(parseInt(args[2]), -6, 6);
            return new PlayerBoostShowdownEvent(stat, -stages);
        });
        registerDecoder("USE_MOVE", args -> {
            if (args.length != 4) return null;
            String move = args[2].toLowerCase();
            int target = parseInt(args[3]);
            return new UseMoveShowdownEvent(move, target);
        });
        registerDecoder("TIMER", args -> {
            if (args.length != 2) return null;
            int time = parseInt(args[1]);
            return new TimerRaidEvent(time);
        });
        registerDecoder("REDUCE_TIMER", args -> {
            if (args.length != 3) return null;
            float ratio = (float) parseDouble(args[2]);
            return new ReduceTimerRaidEvent(ratio);
        });
        registerDecoder("HEAL", args -> {
            if (args.length != 2) return null;
            float ratio = (float) parseDouble(args[1]);
            return new HealRaidEvent(ratio);
        });

        registerStatic("RESET_BOSS", new ResetBossShowdownEvent());
        registerStatic("RESET_PLAYER", new ResetPlayerShowdownEvent());

        registerStatic("SET_RAIN", new SetWeatherShowdownEvent("raindance", false));
        registerStatic("SET_SANDSTORM", new SetWeatherShowdownEvent("sandstorm", false));
        registerStatic("SET_SNOW", new SetWeatherShowdownEvent("snow", false));
        registerStatic("SET_SUN", new SetWeatherShowdownEvent("sunnyday", false));

        registerStatic("SET_ELECTRIC_TERRAIN", new SetTerrainShowdownEvent("electricterrain", false));
        registerStatic("SET_GRASSY_TERRAIN", new SetTerrainShowdownEvent("grassyterrain", false));
        registerStatic("SET_MISTY_TERRAIN", new SetTerrainShowdownEvent("mistyterrain", false));
        registerStatic("SET_PSYCHIC_TERRAIN", new SetTerrainShowdownEvent("psychicterrain", false));

        registerStatic("SHIELD_UP", new ShieldAddShowdownEvent());
        registerStatic("SHIELD_DOWN", new ShieldRemoveShowdownEvent());
    }
}
