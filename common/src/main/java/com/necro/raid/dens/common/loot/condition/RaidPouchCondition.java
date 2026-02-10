package com.necro.raid.dens.common.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.loot.context.RaidLootContexts;
import com.necro.raid.dens.common.loot.predicate.RaidFeaturePredicate;
import com.necro.raid.dens.common.loot.predicate.RaidTierPredicate;
import com.necro.raid.dens.common.loot.predicate.RaidTypePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record RaidPouchCondition(Optional<RaidTierPredicate> tier, Optional<RaidTypePredicate> type, Optional<RaidFeaturePredicate> feature) implements LootItemCondition {
    public static final MapCodec<RaidPouchCondition> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            RaidTierPredicate.CODEC.optionalFieldOf("tier").forGetter(RaidPouchCondition::tier),
            RaidTypePredicate.CODEC.optionalFieldOf("type").forGetter(RaidPouchCondition::type),
            RaidFeaturePredicate.CODEC.optionalFieldOf("feature").forGetter(RaidPouchCondition::feature)
        ).apply(instance, RaidPouchCondition::new));
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);
    @Override
    public @NotNull LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ItemStack itemStack = lootContext.getParam(RaidLootContexts.RAID_POUCH);
        return this.tier().map(p -> p.matches(itemStack)).orElse(true)
            && this.type().map(p -> p.matches(itemStack)).orElse(true)
            && this.feature().map(p -> p.matches(itemStack)).orElse(true);
    }

    public static LootItemCondition.Builder matches(@Nullable RaidTier tier, @Nullable RaidType type, @Nullable RaidFeature feature) {
        return () -> new RaidPouchCondition(
            tier == null ? Optional.empty() : Optional.of(RaidTierPredicate.ofTier(tier)),
            type == null ? Optional.empty() : Optional.of(RaidTypePredicate.ofType(type)),
            feature == null ? Optional.empty() : Optional.of(RaidFeaturePredicate.ofFeature(feature))
        );
    }

    public static LootItemCondition.Builder matches(@Nullable RaidFeature feature) {
        return matches(null, null, feature);
    }
}
