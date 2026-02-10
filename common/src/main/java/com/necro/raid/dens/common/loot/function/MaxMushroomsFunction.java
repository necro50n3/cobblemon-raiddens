package com.necro.raid.dens.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.loot.context.RaidLootContexts;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MaxMushroomsFunction extends LootItemConditionalFunction {
    public static final MapCodec<MaxMushroomsFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance)
        .apply(instance, MaxMushroomsFunction::new));

    protected MaxMushroomsFunction(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return (LootItemFunctionType<? extends LootItemConditionalFunction>) RaidLootFunctions.MAX_MUSHROOMS_FUNCTION.value();
    }

    @Override
    protected @NotNull ItemStack run(@NotNull ItemStack itemStack, @NotNull LootContext lootContext) {
        if (!ModCompat.MEGA_SHOWDOWN.isLoaded()) return itemStack;

        ItemStack raidPouch = lootContext.getParamOrNull(RaidLootContexts.RAID_POUCH);
        if (raidPouch == null) return itemStack;

        RaidTier raidTier = raidPouch.get(ModComponents.TIER_COMPONENT.value());
        if (raidTier == null) return itemStack;
        return RaidDensMSDCompat.getMaxMushroom();
    }

    public static LootItemConditionalFunction.Builder<?> apply() {
        return simpleBuilder(MaxMushroomsFunction::new);
    }
}
