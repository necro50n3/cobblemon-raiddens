package com.necro.raid.dens.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.loot.function.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
         DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CobblemonRaidDens.MOD_ID);

    @SuppressWarnings("unchecked")
    private static <T extends LootItemFunction> Holder<LootItemFunctionType<T>> register(String name, MapCodec<T> mapCodec) {
        return (Holder<LootItemFunctionType<T>>) (Object) LOOT_FUNCTION_TYPES.register(name, () -> new LootItemFunctionType<>(mapCodec));
    }

    public static void registerLootFunctions() {
        RaidLootFunctions.MAX_MUSHROOMS_FUNCTION = register("max_mushrooms_function", MaxMushroomsFunction.CODEC);
        RaidLootFunctions.TERA_SHARDS_FUNCTION = register("tera_shards_function", TeraShardsFunction.CODEC);
        RaidLootFunctions.GEM_TYPE_FUNCTION = register("gem_type_function", GemTypeFunction.CODEC);
        RaidLootFunctions.SHADOW_SHARDS_FUNCTION = register("shadow_shards_function", ShadowShardsFunction.CODEC);
        RaidLootFunctions.SCENTS_FUNCTION = register("scents_function", ScentsFunction.CODEC);
    }
}
