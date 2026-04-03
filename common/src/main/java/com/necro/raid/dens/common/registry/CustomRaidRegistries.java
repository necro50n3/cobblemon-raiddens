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
import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import com.necro.raid.dens.common.registry.custom.*;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;
import com.necro.raid.dens.common.showdown.events.RaidEvents;
import com.necro.raid.dens.common.showdown.events.ShowdownEvents;
import com.necro.raid.dens.common.showdown.events.WithChanceEvent;
import kotlin.Pair;
import net.minecraft.resources.ResourceLocation;
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
        registerAI();
        registerRewardDistributors();
        registerScripts();
    }

    private static void registerAI() {
        AI_REGISTRY.register("random", RandomBattleAI::new);
        AI_REGISTRY.register("strong", () -> new StrongBattleAI(5));
        AI_REGISTRY.register("rct", () -> ModCompat.RCT_API.isLoaded() ? RaidDensRCTCompat.getRctApi() : new StrongBattleAI(5));
    }

    private static void registerRewardDistributors() {
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
    }

    private static void registerScripts() {
        SCRIPT_REGISTRY.register("shield", script -> {
            boolean apply = SCRIPT_REGISTRY.parse(script, "apply", Boolean.class, true);
            return apply ? new ShowdownEvents.ShieldAddShowdownEvent() : new ShowdownEvents.ShieldRemoveShowdownEvent();
        });
        SCRIPT_REGISTRY.register("set_terrain", script -> {
            String terrain = SCRIPT_REGISTRY.parse(script, "terrain", String.class);
            return new ShowdownEvents.SetTerrainShowdownEvent(terrain, false);
        });
        SCRIPT_REGISTRY.register("set_weather", script -> {
            String weather = SCRIPT_REGISTRY.parse(script, "weather", String.class);
            return new ShowdownEvents.SetWeatherShowdownEvent(weather, false);
        });
        SCRIPT_REGISTRY.register("reset_stats", script -> {
            String target = SCRIPT_REGISTRY.parse(script, "target", String.class);
            if ("player".equals(target)) return new ShowdownEvents.ResetPlayerShowdownEvent();
            else if ("boss".equals(target)) return new ShowdownEvents.ResetBossShowdownEvent();
            else throw new JsonSyntaxException("Field \"target\" must be  \"player\" or \"boss\"");
        });
        SCRIPT_REGISTRY.register("modify_catch_rate", script -> {
            float value = SCRIPT_REGISTRY.transform(script, "value", Number.class, Script::toFloat);
            boolean applyToAll = SCRIPT_REGISTRY.parse(script, "apply_to_all", Boolean.class, false);
            String operation = SCRIPT_REGISTRY.parse(script, "operation", String.class);
            if (!Set.of("add", "multiply").contains(operation)) throw new JsonSyntaxException("Field \"operation\" must be \"add\" or \"multiply\"");
            if (applyToAll) return new RaidEvents.ModifyCatchRateAllRaidEvent(value, operation);
            else return new RaidEvents.ModifyCatchRateRaidEvent(value, operation);
        });
        SCRIPT_REGISTRY.register("heal", script -> {
            float value = SCRIPT_REGISTRY.transform(script, "value", Number.class, Script::toFloat);
            return new RaidEvents.HealRaidEvent(value);
        });
        SCRIPT_REGISTRY.register("reduce_timer", script -> {
            float value = SCRIPT_REGISTRY.transform(script, "value", Number.class, Script::toFloat);
            return new RaidEvents.ReduceTimerRaidEvent(value);
        });
        SCRIPT_REGISTRY.register("timer", script -> {
            int value = SCRIPT_REGISTRY.transform(script, "value", Number.class, Script::toInt);
            return new RaidEvents.TimerRaidEvent(value);
        });
        SCRIPT_REGISTRY.register("use_move", script -> {
            String move = SCRIPT_REGISTRY.parse(script, "move", String.class);
            int target = SCRIPT_REGISTRY.transform(script, "target", Number.class, Script::toInt);
            return new ShowdownEvents.UseMoveShowdownEvent(move, target);
        });
        SCRIPT_REGISTRY.register("player_stats", script -> {
            Map<?, ?> map = SCRIPT_REGISTRY.parse(script, "stats", Map.class);
            Map<Stat, Integer> stats = map.entrySet().stream()
                .filter(e -> e.getKey() instanceof String string && STAT_MAP.containsKey(string))
                .filter(e -> e.getValue() instanceof Number)
                .collect(Collectors.toMap(e -> STAT_MAP.get((String) e.getKey()), e -> -Script.toInt(e.getValue())));
            if (stats.isEmpty()) throw new JsonSyntaxException("Failed to parse field \"stats\"");
            return new ShowdownEvents.PlayerMapBoostShowdownEvent(stats);
        });
        SCRIPT_REGISTRY.register("boss_stats", script -> {
            Map<?, ?> map = SCRIPT_REGISTRY.parse(script, "stats", Map.class);
            Map<Stat, Integer> stats = map.entrySet().stream()
                .filter(e -> e.getKey() instanceof String string && STAT_MAP.containsKey(string))
                .filter(e -> e.getValue() instanceof Number)
                .collect(Collectors.toMap(e -> STAT_MAP.get((String) e.getKey()), e -> Script.toInt(e.getValue())));
            if (stats.isEmpty()) throw new JsonSyntaxException("Failed to parse field \"stats\"");
            return new ShowdownEvents.RaidMapBoostShowdownEvent(stats);
        });
        SCRIPT_REGISTRY.register("forme_change", script -> {
            String form = SCRIPT_REGISTRY.parse(script, "form", String.class);
            return new ShowdownEvents.FormeChangeShowdownEvent(form);
        });
        SCRIPT_REGISTRY.register("mega_evolve", script -> new ShowdownEvents.MegaEvolveShowdownEvent());
        SCRIPT_REGISTRY.register("dynamax", script -> new ShowdownEvents.DynamaxShowdownEvent());
        SCRIPT_REGISTRY.register("terastallize", script -> new ShowdownEvents.TerastallizeShowdownEvent());
        SCRIPT_REGISTRY.register("add_script", script -> {
            String trigger = SCRIPT_REGISTRY.parse(script, "trigger", String.class);
            List<AbstractEvent> events = SCRIPT_REGISTRY.decodeList(script.get("scripts"));
            RaidTrigger<?> raidTrigger = RaidTriggerType.decode(trigger, events);
            if (raidTrigger == null) throw new JsonSyntaxException("Failed to parse field \"trigger\"");
            return new RaidEvents.AddScriptRaidEvent(raidTrigger);
        });
        SCRIPT_REGISTRY.register("scale", script -> {
            float scale = SCRIPT_REGISTRY.transform(script, "scale", Number.class, Script::toFloat);
            if (scale <= 0F) throw new JsonSyntaxException("Cannot have negative field \"scale\"");
            float rate = SCRIPT_REGISTRY.transform(script, "rate", Number.class, Script::toFloat, 1F / 20F);
            if (rate <= 0F) throw new JsonSyntaxException("Cannot have negative field \"rate\"");
            return new RaidEvents.ScaleBossRaidEvent(scale, rate);
        });
        SCRIPT_REGISTRY.register("play_sound", script -> {
            String soundId = SCRIPT_REGISTRY.parse(script, "sound", String.class);
            ResourceLocation sound;
            try { sound = ResourceLocation.parse(soundId); }
            catch (Exception e) { throw new JsonSyntaxException("Failed to parse field \"sound\""); }
            boolean isMusic = SCRIPT_REGISTRY.parse(script, "is_music", Boolean.class, true);
            return new RaidEvents.PlaySoundRaidEvent(sound, isMusic);
        });
        SCRIPT_REGISTRY.register("with_chance", script -> {
            float chance = SCRIPT_REGISTRY.transform(script, "chance", Number.class, Script::toFloat);
            List<AbstractEvent> events = SCRIPT_REGISTRY.decodeList(script.get("scripts"));
            return new WithChanceEvent(chance, events);
        });
    }
}
