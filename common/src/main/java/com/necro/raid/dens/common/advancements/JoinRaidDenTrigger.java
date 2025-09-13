package com.necro.raid.dens.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class JoinRaidDenTrigger extends SimpleCriterionTrigger<JoinRaidDenTrigger.TriggerInstance> {
    public JoinRaidDenTrigger() {}

    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, (triggerInstance) -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<JoinRaidDenTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
            ).apply(instance, JoinRaidDenTrigger.TriggerInstance::new));

        public static Criterion<TriggerInstance> joinRaidDen() {
            return ((JoinRaidDenTrigger) RaidDenCriteriaTriggers.JOIN_RAID_DEN.value()).createCriterion(new TriggerInstance(Optional.empty()));
        }
    }
}
