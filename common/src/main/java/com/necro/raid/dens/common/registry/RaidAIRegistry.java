package com.necro.raid.dens.common.registry;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.rctapi.RaidDensRCTCompat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Supplier;

public class RaidAIRegistry {
    private static final Object2ObjectOpenHashMap<String, Supplier<BattleAI>> REGISTRY = new Object2ObjectOpenHashMap<>();
    private static boolean FROZEN = false;

    public static void register(String id, Supplier<BattleAI> factory) {
        if (FROZEN) throw new IllegalStateException("Attempted to register Raid AI after initialization.");
        REGISTRY.put(id.toLowerCase(), factory);
    }

    public static void freeze() {
        FROZEN = true;
        REGISTRY.trim();
    }

    public static void init() {
        register("random", RandomBattleAI::new);
        register("strong", () -> new StrongBattleAI(5));
        register("rct", () -> ModCompat.RCT_API.isLoaded() ? RaidDensRCTCompat.getRctApi() : new StrongBattleAI(5));
    }

    public static BattleAI build(String key) {
        return REGISTRY.getOrDefault(key.toLowerCase(), RandomBattleAI::new).get();
    }
}
