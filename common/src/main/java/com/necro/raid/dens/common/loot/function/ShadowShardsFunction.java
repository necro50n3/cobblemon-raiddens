package com.necro.raid.dens.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.shadowedhearts.RaidDensShadowedHeartsCompat;
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

public class ShadowShardsFunction extends LootItemConditionalFunction {
    public static final MapCodec<ShadowShardsFunction> CODEC = RecordCodecBuilder.mapCodec((instance) -> commonFields(instance)
        .apply(instance, ShadowShardsFunction::new));

    protected ShadowShardsFunction(List<LootItemCondition> list) {
        super(list);
    }

    @Override
    public @NotNull LootItemFunctionType<ShadowShardsFunction> getType() {
        return RaidLootFunctions.SHADOW_SHARDS_FUNCTION.value();
    }

    @Override
    protected @NotNull ItemStack run(@NotNull ItemStack itemStack, @NotNull LootContext lootContext) {
        if (!ModCompat.SHADOWED_HEARTS.isLoaded()) return itemStack;

        ItemStack raidPouch = lootContext.getParamOrNull(RaidLootContexts.RAID_POUCH);
        if (raidPouch == null) return itemStack;

        RaidTier raidTier = raidPouch.get(ModComponents.TIER_COMPONENT.value());
        if (raidTier == null) return itemStack;
        return RaidDensShadowedHeartsCompat.getShadowShard();
    }

    public static Builder<?> apply() {
        return simpleBuilder(ShadowShardsFunction::new);
    }
}
