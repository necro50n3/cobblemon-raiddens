package com.necro.raid.dens.common.registry;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.google.gson.JsonSyntaxException;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.rctapi.RaidDensRCTCompat;
import com.necro.raid.dens.common.data.raid.Script;
import com.necro.raid.dens.common.raids.rewards.RewardDistributor;
import com.necro.raid.dens.common.registry.custom.ScriptRegistry;
import com.necro.raid.dens.common.registry.custom.StringRegistry;
import com.necro.raid.dens.common.showdown.events.*;
import kotlin.Pair;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CustomRaidRegistries {
    public static final StringRegistry<Supplier<BattleAI>> AI_REGISTRY = new StringRegistry<>("random");
    public static final StringRegistry<RewardDistributor> REWARD_DIST_REGISTRY = new StringRegistry<>("random");
    public static final ScriptRegistry SCRIPT_REGISTRY = new ScriptRegistry();

    private static final Map<String, Stat> STAT_MAP = Map.of(
        "atk", Stats.ATTACK,
        "def", Stats.DEFENCE,
        "spa", Stats.SPECIAL_ATTACK,
        "spd", Stats.SPECIAL_DEFENCE,
        "spe", Stats.SPEED,
        "acc", Stats.ACCURACY,
        "eva", Stats.EVASION
    );

    public static void freeze() {
        AI_REGISTRY.freeze();
        REWARD_DIST_REGISTRY.freeze();
        SCRIPT_REGISTRY.freeze();
    }

    public static void registerDefaults() {
        AI_REGISTRY.register("random", RandomBattleAI::new);
        AI_REGISTRY.register("strong", () -> new StrongBattleAI(5));
        AI_REGISTRY.register("rct", () -> ModCompat.RCT_API.isLoaded() ? RaidDensRCTCompat.getRctApi() : new StrongBattleAI(5));

        REWARD_DIST_REGISTRY.register("random", (players, raid) -> {
            int maxCatches = raid.getRaidBoss().getMaxCatches();
            if (maxCatches < 0 || players.size() < maxCatches) return new Pair<>(players, List.of());

            Collections.shuffle(players);
            List<ServerPlayer> success = players.subList(0, maxCatches);
            List<ServerPlayer> failed = players.subList(maxCatches, players.size());
            return new Pair<>(success, failed);
        });
        REWARD_DIST_REGISTRY.register("damage", (players, raid) -> {
            int maxCatches = raid.getRaidBoss().getMaxCatches();
            if (maxCatches < 0 || players.size() < maxCatches) return new Pair<>(players, List.of());

            players.sort((a, b) -> Float.compare(raid.getDamage(b), raid.getDamage(a)));
            List<ServerPlayer> success = players.subList(0, maxCatches);
            List<ServerPlayer> failed = players.subList(maxCatches, players.size());
            return new Pair<>(success, failed);
        });
        REWARD_DIST_REGISTRY.register("survivor", (players, raid) -> {
            int maxCatches = raid.getRaidBoss().getMaxCatches();
            List<ServerPlayer> success = new ArrayList<>();
            List<ServerPlayer> failed = new ArrayList<>();
            for (ServerPlayer player : players) {
                if (raid.hasFailed(player)) failed.add(player);
                else success.add(player);
            }

            if (maxCatches > 0 && success.size() > maxCatches) {
                Collections.shuffle(success);
                failed.addAll(success.subList(maxCatches, success.size()));
                success = success.subList(0, maxCatches);
            }

            return new Pair<>(success, failed);
        });

        SCRIPT_REGISTRY.register("shield", script -> {
            boolean apply = (boolean) script.getOrDefault("apply", true);
            return apply ? new ShieldAddShowdownEvent() : new ShieldRemoveShowdownEvent();
        });
        SCRIPT_REGISTRY.register("set_terrain", script -> {
            String terrain = (String) script.get("terrain");
            if (terrain == null) throw new JsonSyntaxException("Missing field \"terrain\"");
            return new SetTerrainShowdownEvent(terrain, false);
        });
        SCRIPT_REGISTRY.register("set_weather", script -> {
            String weather = (String) script.get("weather");
            if (weather == null) throw new JsonSyntaxException("Missing field \"weather\"");
            return new SetWeatherShowdownEvent(weather, false);
        });
        SCRIPT_REGISTRY.register("reset_stats", script -> {
            String target = (String) script.get("target");
            if ("player".equals(target)) return new ResetPlayerShowdownEvent();
            else if ("boss".equals(target)) return new ResetBossShowdownEvent();
            else throw new JsonSyntaxException("Missing or incorrect field \"target\"");
        });
        SCRIPT_REGISTRY.register("modify_catch_rate", script -> {
            if (!script.containsKey("value")) throw new JsonSyntaxException("Missing field \"value\"");
            float value = Script.toFloat(script.get("value"));
            boolean applyToAll = (boolean) script.getOrDefault("apply_to_all", false);
            String operation = (String) script.get("operation");
            if (!Set.of("add", "multiply").contains(operation)) throw new JsonSyntaxException("Missing or incorrect field \"operation\"");
            if (applyToAll) return new ModifyCatchRateAllRaidEvent(value, operation);
            else return new ModifyCatchRateRaidEvent(value, operation);
        });
        SCRIPT_REGISTRY.register("heal", script -> {
            if (!script.containsKey("value")) throw new JsonSyntaxException("Missing field \"value\"");
            float value = Script.toFloat(script.get("value"));
            return new HealRaidEvent(value);
        });
        SCRIPT_REGISTRY.register("reduce_timer", script -> {
            if (!script.containsKey("value")) throw new JsonSyntaxException("Missing field \"value\"");
            float value = Script.toFloat(script.get("value"));
            return new ReduceTimerRaidEvent(value);
        });
        SCRIPT_REGISTRY.register("timer", script -> {
            if (!script.containsKey("value")) throw new JsonSyntaxException("Missing field \"value\"");
            int value = Script.toInt(script.get("value"));
            return new TimerRaidEvent(value);
        });
        SCRIPT_REGISTRY.register("use_move", script -> {
            String move = (String) script.get("move");
            if (move == null) throw new JsonSyntaxException("Missing field \"move\"");
            if (!script.containsKey("target")) throw new JsonSyntaxException("Missing field \"target\"");
            int target = Script.toInt(script.get("target"));
            return new UseMoveShowdownEvent(move, target);
        });
        SCRIPT_REGISTRY.register("player_stat", script -> {
            Object map = script.get("stats");
            if (map == null) throw new JsonSyntaxException("Missing field \"stats\"");
            Map<Stat, Integer> stats = ((Map<?, ?>) script.get("stats")).entrySet().stream()
                .filter(e -> e.getKey() instanceof String string && STAT_MAP.containsKey(string))
                .filter(e -> e.getValue() instanceof Number)
                .collect(Collectors.toMap(e -> STAT_MAP.get((String) e.getKey()), e -> -Script.toInt(e.getValue())));
            if (stats.isEmpty()) throw new JsonSyntaxException("Invalid field \"stats\"");
            return new PlayerMapBoostShowdownEvent(stats);
        });
        SCRIPT_REGISTRY.register("boss_stat", script -> {
            Object map = script.get("stats");
            if (map == null) throw new JsonSyntaxException("Missing field \"stats\"");
            Map<Stat, Integer> stats = ((Map<?, ?>) script.get("stats")).entrySet().stream()
                .filter(e -> e.getKey() instanceof String string && STAT_MAP.containsKey(string))
                .filter(e -> e.getValue() instanceof Number)
                .collect(Collectors.toMap(e -> STAT_MAP.get((String) e.getKey()), e -> Script.toInt(e.getValue())));
            if (stats.isEmpty()) throw new JsonSyntaxException("Invalid field \"stats\"");
            return new RaidMapBoostShowdownEvent(stats);
        });
    }
}
