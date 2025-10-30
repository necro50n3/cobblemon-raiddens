package com.necro.raid.dens.common.mixins.ai;

import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.cobblemon.mod.common.battles.ai.strongBattleAI.TrackerPokemon;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(StrongBattleAI.class)
public abstract class StrongBattleAIMixin {
    @Inject(method = "calculateDamage(Lcom/cobblemon/mod/common/battles/InBattleMove;Lcom/cobblemon/mod/common/battles/ai/strongBattleAI/TrackerPokemon;Lcom/cobblemon/mod/common/battles/ai/strongBattleAI/TrackerPokemon;)D", at = @At("HEAD"), remap = false, cancellable = true)
    private void calculateDamageInject(InBattleMove move, TrackerPokemon pokemon, TrackerPokemon opponent, CallbackInfoReturnable<Double> cir) {
        if (
            pokemon.getPokemon() != null
            && pokemon.getPokemon().getEntity() != null
            && ((IRaidAccessor) pokemon.getPokemon().getEntity()).isRaidBoss()
            && move.id.equals("lastresort")
        ) cir.setReturnValue(0.0);
    }
}
