package com.necro.raid.dens.fabricgen.loot;

import com.mojang.serialization.MapCodec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.loot.function.RaidLootFunctions;
import com.necro.raid.dens.common.loot.function.GemTypeFunction;
import com.necro.raid.dens.common.loot.function.MaxMushroomsFunction;
import com.necro.raid.dens.common.loot.function.TeraShardsFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class FabricLootFunctions {
    private static Holder<LootItemFunctionType<?>> register(String name, MapCodec<? extends LootItemConditionalFunction> mapCodec) {
        return Holder.direct(Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, name), new LootItemFunctionType<>(mapCodec)));
    }

    public static void registerLootFunctions() {
        RaidLootFunctions.MAX_MUSHROOMS_FUNCTION = register("max_mushrooms_function", MaxMushroomsFunction.CODEC);
        RaidLootFunctions.TERA_SHARDS_FUNCTION = register("tera_shards_function", TeraShardsFunction.CODEC);
        RaidLootFunctions.GEM_TYPE_FUNCTION = register("gem_type_function", GemTypeFunction.CODEC);
    }
}
