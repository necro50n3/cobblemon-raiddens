package com.necro.raid.dens.neoforge.mixins.ai;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;
import com.necro.raid.dens.common.compat.rctapi.RCTBattleAIImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RCTBattleAI.class)
public abstract class RCTBattleAIMixin {
    @Shadow(remap = false)
    protected abstract double evalMove(BattlePokemon from, BattlePokemon to, InBattleMove move);

    @Inject(method = "choose", at = @At("HEAD"), remap = false, cancellable = true)
    private void chooseInject(ActiveBattlePokemon pokemon, PokemonBattle battle, BattleSide aiSide, ShowdownMoveset moveset, boolean forceSwitch, CallbackInfoReturnable<ShowdownActionResponse> cir) {
        RCTBattleAIImpl.choose(pokemon, moveset, this::evalMove, cir);
    }

    @Inject(method = "evalMove", at = @At("HEAD"), remap = false, cancellable = true)
    private void evalMoveInject(BattlePokemon from, BattlePokemon _to, InBattleMove move, CallbackInfoReturnable<Double> cir) {
        RCTBattleAIImpl.evalMove(move, cir);
    }
}
