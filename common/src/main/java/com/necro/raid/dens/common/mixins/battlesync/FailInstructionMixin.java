package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.FailInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.registry.RaidSupportRegistry;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FailInstruction.class)
public abstract class FailInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke$lambda$0", at = @At("HEAD"), remap = false, cancellable = true)
    private static void invokeInject2(FailInstruction this0, PokemonBattle battle, CallbackInfoReturnable<Unit> cir) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        BattlePokemon battlePokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return;

        BattlePokemon userPokemon = this0.getMessage().battlePokemon(0, battle);
        if (userPokemon != null && RaidSupportRegistry.removeFromQueue(userPokemon.getUuid())) cir.setReturnValue(Unit.INSTANCE);
    }
}
