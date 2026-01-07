package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.dispatch.*;
import com.cobblemon.mod.common.battles.interpreter.instructions.DamageInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageInstruction.class)
public abstract class DamageInstructionMixin implements ActionEffectInstruction {
    @Final
    @Shadow(remap = false)
    private BattleMessage publicMessage;

    @Final
    @Shadow(remap = false)
    private BattleActor actor;

    @Inject(method = "postActionEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private void postActionEffectInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).isRaidBattle()) return;
        RaidInstance raidInstance = ((IRaidBattle) battle).getRaidBattle();
        BattlePokemon battlePokemon = this.publicMessage.battlePokemon(0, this.actor.battle);
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).isRaidBoss()) return;
        else if (raidInstance.getRemainingHealth() == 0) {
            battle.dispatch(DispatchResultKt::getGO);
            ci.cancel();
        }
    }
}
