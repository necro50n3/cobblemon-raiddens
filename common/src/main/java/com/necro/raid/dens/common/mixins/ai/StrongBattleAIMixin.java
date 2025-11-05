package com.necro.raid.dens.common.mixins.ai;

import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ai.ActiveTracker;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.necro.raid.dens.common.raids.RaidAI;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(StrongBattleAI.class)
public abstract class StrongBattleAIMixin {
    @Inject(method = "calculateDamage", at = @At("HEAD"), remap = false, cancellable = true)
    private void calculateDamageInject(InBattleMove move, ActiveTracker.TrackerPokemon pokemon, ActiveTracker.TrackerPokemon opponent, String currentWeather, CallbackInfoReturnable<Double> cir) {
        if (
            pokemon.component1() != null
            && pokemon.component1().getEntity() != null
            && ((IRaidAccessor) pokemon.component1().getEntity()).isRaidBoss()
            && RaidAI.BLOCKED_MOVES.contains(move.id)
        ) cir.setReturnValue(0.0);
    }
}
