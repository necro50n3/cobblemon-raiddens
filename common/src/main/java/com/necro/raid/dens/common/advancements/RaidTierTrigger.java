package com.necro.raid.dens.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RaidTierTrigger extends SimpleCriterionTrigger<RaidTierTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<RaidTierTrigger.TriggerInstance> codec() {
        return RaidTierTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, RaidTier tier) {
        this.trigger(serverPlayer, (triggerInstance) -> triggerInstance.matches(tier));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<RaidTier> tier) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<RaidTierTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RaidTierTrigger.TriggerInstance::player),
            RaidTier.codec().optionalFieldOf("tier").forGetter(RaidTierTrigger.TriggerInstance::tier)
        ).apply(instance, RaidTierTrigger.TriggerInstance::new));

        public boolean matches(RaidTier raidTier) {
            return this.tier.isPresent() && this.tier.get() == raidTier;
        }

        public static Criterion<TriggerInstance> tier(RaidTier tier) {
            return ((RaidTierTrigger) RaidDenCriteriaTriggers.RAID_TIER.value()).createCriterion(new RaidTierTrigger.TriggerInstance(Optional.empty(), Optional.of(tier)));
        }
    }
}
