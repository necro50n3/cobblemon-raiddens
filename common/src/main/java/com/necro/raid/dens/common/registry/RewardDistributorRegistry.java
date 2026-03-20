package com.necro.raid.dens.common.registry;

import com.necro.raid.dens.common.raids.rewards.RewardDistributor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kotlin.Pair;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardDistributorRegistry {
    private static final Object2ObjectOpenHashMap<String, RewardDistributor> REGISTRY = new Object2ObjectOpenHashMap<>();
    private static boolean FROZEN = false;

    public static void register(String id, RewardDistributor distributor) {
        if (FROZEN) throw new IllegalStateException("Attempted to register Reward Distributor after initialization.");
        REGISTRY.put(id.toLowerCase(), distributor);
    }

    public static void freeze() {
        FROZEN = true;
        REGISTRY.trim();
    }

    public static void init() {
        register("random", (players, raid) -> {
            int maxCatches = raid.getRaidBoss().getMaxCatches();
            if (maxCatches < 0 || players.size() < maxCatches) return new Pair<>(players, List.of());

            Collections.shuffle(players);
            List<ServerPlayer> success = players.subList(0, maxCatches);
            List<ServerPlayer> failed = players.subList(maxCatches, players.size());
            return new Pair<>(success, failed);
        });

        register("damage", (players, raid) -> {
            int maxCatches = raid.getRaidBoss().getMaxCatches();
            if (maxCatches < 0 || players.size() < maxCatches) return new Pair<>(players, List.of());

            players.sort((a, b) -> Float.compare(raid.getDamage(b), raid.getDamage(a)));
            List<ServerPlayer> success = players.subList(0, maxCatches);
            List<ServerPlayer> failed = players.subList(maxCatches, players.size());
            return new Pair<>(success, failed);
        });

        register("survivor", (players, raid) -> {
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

    public static RewardDistributor get(String key) {
        return REGISTRY.getOrDefault(key.toLowerCase(), REGISTRY.get("random"));
    }
}
