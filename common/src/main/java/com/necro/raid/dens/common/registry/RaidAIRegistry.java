package com.necro.raid.dens.common.registry;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.rctapi.RaidDensRCTCompat;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class RaidAIRegistry {
    private static final Object2ObjectOpenHashMap<ResourceLocation, Supplier<BattleAI>> REGISTRY = new Object2ObjectOpenHashMap<>();
    private static boolean FROZEN = false;

    public static void register(ResourceLocation id, Supplier<BattleAI> factory) {
        if (FROZEN) throw new IllegalStateException("Attempted to register Raid AI after initialization.");
        REGISTRY.put(id, factory);
    }

    public static void freeze() {
        FROZEN = true;
        REGISTRY.trim();
    }

    public static void init() {
        register(ResourceLocation.parse("cobblemon:random"), RandomBattleAI::new);
        register(ResourceLocation.parse("cobblemon:strong"), () -> new StrongBattleAI(5));
        register(ResourceLocation.parse("rctapi:rct"), () -> ModCompat.RCT_API.isLoaded() ? RaidDensRCTCompat.getRctApi() : new StrongBattleAI(5));
    }

    public static BattleAI build(ResourceLocation key) {
        return REGISTRY.getOrDefault(key, RandomBattleAI::new).get();
    }
}
