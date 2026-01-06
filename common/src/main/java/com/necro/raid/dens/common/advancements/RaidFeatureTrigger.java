package com.necro.raid.dens.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RaidFeatureTrigger extends SimpleCriterionTrigger<RaidFeatureTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<RaidFeatureTrigger.TriggerInstance> codec() {
        return RaidFeatureTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, RaidFeature feature) {
        this.trigger(serverPlayer, (triggerInstance) -> triggerInstance.matches(feature));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<RaidFeature> feature) implements SimpleInstance {
        public static final Codec<RaidFeatureTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RaidFeatureTrigger.TriggerInstance::player),
            RaidFeature.codec().optionalFieldOf("feature").forGetter(RaidFeatureTrigger.TriggerInstance::feature)
        ).apply(instance, RaidFeatureTrigger.TriggerInstance::new));

        public boolean matches(RaidFeature feature) {
            if (!ModCompat.MEGA_SHOWDOWN.isLoaded()) return false;
            return this.feature.isPresent() && this.feature.get() == feature;
        }

        public static Criterion<TriggerInstance> feature(RaidFeature feature) {
            return ((RaidFeatureTrigger) RaidDenCriteriaTriggers.RAID_FEATURE.value()).createCriterion(new RaidFeatureTrigger.TriggerInstance(Optional.empty(), Optional.of(feature)));
        }
    }
}
