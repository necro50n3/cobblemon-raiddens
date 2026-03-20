package com.necro.raid.dens.common.registry;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.rctapi.RaidDensRCTCompat;
import com.necro.raid.dens.common.raids.rewards.RewardDistributor;
import com.necro.raid.dens.common.registry.custom.CustomRegistry;
import com.necro.raid.dens.common.registry.custom.StringRegistry;
import kotlin.Pair;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CustomRaidRegistries {
    public static final CustomRegistry<String, Supplier<BattleAI>> AI_REGISTRY = new StringRegistry<>("random");
    public static final CustomRegistry<String, RewardDistributor> REWARD_DIST_REGISTRY = new StringRegistry<>("random");

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
    }
}
