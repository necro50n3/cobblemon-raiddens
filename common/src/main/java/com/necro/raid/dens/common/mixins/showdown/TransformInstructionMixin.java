package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.TransformInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransformInstruction.class)
public class TransformInstructionMixin {
    @Final
    @Shadow(remap = false)
    private BattleMessage message;

    @Inject(method = "invoke", at = @At("HEAD"), cancellable = true, remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        BattlePokemon pokemon = this.message.battlePokemon(0, battle);
        if (pokemon == null || pokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) pokemon.getEntity()).crd_isRaidBoss()) return;
        if (pokemon.getEntity().getEffects().getMockEffect() != null) ci.cancel();
    }
}
