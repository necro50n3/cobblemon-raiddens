package com.necro.raid.dens.common.loot.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.loot.context.RaidLootContexts;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BonusLootCondition(Optional<Boolean> apply) implements LootItemCondition {
    public static final MapCodec<BonusLootCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.BOOL.optionalFieldOf("apply").forGetter(BonusLootCondition::apply)
        ).apply(instance, BonusLootCondition::new));
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

    @Override
    public @NotNull LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        boolean apply = lootContext.getParam(RaidLootContexts.BONUS_LOOT);
        return this.apply().map(p -> p == apply).orElse(false);
    }

    public static Builder matches(@Nullable Boolean apply) {
        return () -> new BonusLootCondition(apply == null ? Optional.empty() : Optional.of(apply));
    }
}
