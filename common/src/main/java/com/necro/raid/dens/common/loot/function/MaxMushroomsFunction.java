package com.necro.raid.dens.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.loot.LootFunctions;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
        return (LootItemFunctionType<? extends LootItemConditionalFunction>) LootFunctions.MAX_MUSHROOMS_FUNCTION.value();
    }

    @Override
    protected @NotNull ItemStack run(@NotNull ItemStack itemStack, @NotNull LootContext lootContext) {
        if (!ModCompat.MEGA_SHOWDOWN.isLoaded()) return itemStack;

        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof LivingEntity livingEntity)) return itemStack;
        ItemStack heldItem = livingEntity.getMainHandItem();

        RaidTier raidTier;
        if (heldItem.has(ModComponents.TIER_COMPONENT.value())) {
            raidTier = heldItem.get(ModComponents.TIER_COMPONENT.value());
        }
        else return itemStack;
        assert raidTier != null;

        int count = switch (raidTier) {
            case RaidTier.TIER_ONE, RaidTier.TIER_TWO -> lootContext.getRandom().nextIntBetweenInclusive(0, 1);
            case RaidTier.TIER_THREE, RaidTier.TIER_FOUR -> lootContext.getRandom().nextIntBetweenInclusive(0, 2);
            case RaidTier.TIER_FIVE -> lootContext.getRandom().nextIntBetweenInclusive(1, 2);
            case RaidTier.TIER_SIX -> lootContext.getRandom().nextIntBetweenInclusive(2, 3);
            default -> 3;
        };
        if (count == 0) return itemStack;
        itemStack = RaidDensMSDCompat.getMaxMushroom();
        itemStack.setCount(count);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> apply() {
        return simpleBuilder(MaxMushroomsFunction::new);
    }
}
