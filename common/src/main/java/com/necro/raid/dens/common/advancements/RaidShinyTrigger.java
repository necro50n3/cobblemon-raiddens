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

public class RaidShinyTrigger extends SimpleCriterionTrigger<RaidShinyTrigger.TriggerInstance> {
    @Override
    public @NotNull Codec<RaidShinyTrigger.TriggerInstance> codec() {
        return RaidShinyTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, boolean shiny) {
        this.trigger(serverPlayer, (triggerInstance) -> triggerInstance.matches(shiny));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Boolean> shiny) implements SimpleInstance {
        public static final Codec<RaidShinyTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(RaidShinyTrigger.TriggerInstance::player),
            Codec.BOOL.optionalFieldOf("shiny").forGetter(RaidShinyTrigger.TriggerInstance::shiny)
        ).apply(instance, RaidShinyTrigger.TriggerInstance::new));

        public boolean matches(boolean shiny) {
            return this.shiny.isPresent() && this.shiny.get() == shiny;
        }

        public static Criterion<TriggerInstance> shiny(boolean shiny) {
            return ((RaidShinyTrigger) RaidDenCriteriaTriggers.RAID_SHINY.value()).createCriterion(new RaidShinyTrigger.TriggerInstance(Optional.empty(), Optional.of(shiny)));
        }
    }
}
