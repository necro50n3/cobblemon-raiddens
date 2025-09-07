package com.necro.raid.dens.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.loot.LootFunctions;
import com.necro.raid.dens.common.loot.function.GemTypeFunction;
import com.necro.raid.dens.common.loot.function.MaxMushroomsFunction;
import com.necro.raid.dens.common.loot.function.TeraShardsFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeLootFunctions {
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
         DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, CobblemonRaidDens.MOD_ID);

    private static Holder<LootItemFunctionType<?>> register(String name, MapCodec<? extends LootItemConditionalFunction> mapCodec) {
        return LOOT_FUNCTION_TYPES.register(name, () -> new LootItemFunctionType<>(mapCodec));
    }

    public static void registerLootFunctions() {
        LootFunctions.MAX_MUSHROOMS_FUNCTION = register("max_mushrooms_function", MaxMushroomsFunction.CODEC);
        LootFunctions.TERA_SHARDS_FUNCTION = register("tera_shards_function", TeraShardsFunction.CODEC);
        LootFunctions.GEM_TYPE_FUNCTION = register("gem_type_function", GemTypeFunction.CODEC);
    }
}
