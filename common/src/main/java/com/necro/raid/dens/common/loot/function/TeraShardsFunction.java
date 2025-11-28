package com.necro.raid.dens.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.loot.LootFunctions;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
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

public class TeraShardsFunction extends LootItemConditionalFunction {
    public static final MapCodec<TeraShardsFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance)
        .apply(instance, TeraShardsFunction::new));

    protected TeraShardsFunction(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
        return (LootItemFunctionType<? extends LootItemConditionalFunction>) LootFunctions.TERA_SHARDS_FUNCTION.value();
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

        RaidType raidType;
        if (heldItem.has(ModComponents.TYPE_COMPONENT.value())) {
            raidType = heldItem.get(ModComponents.TYPE_COMPONENT.value());
        }
        else return itemStack;
        assert raidTier != null && raidType != null;
        if (raidType == RaidType.NONE) return itemStack;

        int count = switch (raidTier) {
            case RaidTier.TIER_ONE, RaidTier.TIER_TWO -> lootContext.getRandom().nextIntBetweenInclusive(1, 3);
            case RaidTier.TIER_THREE, RaidTier.TIER_FOUR -> lootContext.getRandom().nextIntBetweenInclusive(2, 5);
            case RaidTier.TIER_FIVE -> lootContext.getRandom().nextIntBetweenInclusive(5, 10);
            case RaidTier.TIER_SIX -> lootContext.getRandom().nextIntBetweenInclusive(10, 20);
            default -> 30;
        };
        if (count == 0) return itemStack;
        itemStack = RaidDensMSDCompat.getTeraShard(raidType);
        itemStack.setCount(count);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> apply() {
        return simpleBuilder(TeraShardsFunction::new);
    }
}
