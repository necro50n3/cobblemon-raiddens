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

public class RaidAdventureTrigger extends SimpleCriterionTrigger<RaidAdventureTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<RaidAdventureTrigger.TriggerInstance> codec() {
        return RaidAdventureTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, boolean adventure) {
        this.trigger(serverPlayer, (triggerInstance) -> triggerInstance.matches(adventure));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Boolean> adventure) implements SimpleInstance {
        public static final Codec<RaidAdventureTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RaidAdventureTrigger.TriggerInstance::player),
            Codec.BOOL.optionalFieldOf("adventure").forGetter(RaidAdventureTrigger.TriggerInstance::adventure)
        ).apply(instance, RaidAdventureTrigger.TriggerInstance::new));

        public boolean matches(boolean adventure) {
            return this.adventure.isPresent() && this.adventure.get() == adventure;
        }

        public static Criterion<TriggerInstance> adventure(boolean adventure) {
            return RaidDenCriteriaTriggers.RAID_ADVENTURE.value().createCriterion(new RaidAdventureTrigger.TriggerInstance(Optional.empty(), Optional.of(adventure)));
        }
    }
}
