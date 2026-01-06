package com.necro.raid.dens.common.mixins.ai;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.cobblemon.mod.common.battles.ai.strongBattleAI.TrackerPokemon;
import com.necro.raid.dens.common.data.raid.RaidAI;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StrongBattleAI.class)
public abstract class StrongBattleAIMixin {
    @Inject(method = "calculateDamage(Lcom/cobblemon/mod/common/battles/InBattleMove;Lcom/cobblemon/mod/common/battles/ai/strongBattleAI/TrackerPokemon;Lcom/cobblemon/mod/common/battles/ai/strongBattleAI/TrackerPokemon;)D", at = @At("HEAD"), remap = false, cancellable = true)
    private void calculateDamageInject(InBattleMove move, TrackerPokemon pokemon, TrackerPokemon opponent, CallbackInfoReturnable<Double> cir) {
        if (
            pokemon.getPokemon() != null
                && pokemon.getPokemon().getEntity() != null
                && ((IRaidAccessor) pokemon.getPokemon().getEntity()).isRaidBoss()
            && RaidAI.BLOCKED_MOVES.contains(move.id)
        ) cir.setReturnValue(0.0);
        else if (!move.canBeUsed()) cir.setReturnValue(0.0);
    }

    @Inject(method = "choose", at = @At("RETURN"), remap = false, cancellable = true)
    private void chooseMoveInject(ActiveBattlePokemon pokemon, PokemonBattle battle, BattleSide aiSide, ShowdownMoveset moveset, boolean forceSwitch, CallbackInfoReturnable<ShowdownActionResponse> cir) {
        if (
            pokemon.getBattlePokemon() != null
            && pokemon.getBattlePokemon().getEntity() != null
            && ((IRaidAccessor) pokemon.getBattlePokemon().getEntity()).isRaidBoss()
        ) {
            ShowdownActionResponse response = cir.getReturnValue();
            if (!(response instanceof MoveActionResponse move)) return;
            else if (!RaidAI.BLOCKED_MOVES.contains(move.getMoveName())) return;
            cir.setReturnValue(PassActionResponse.INSTANCE);
        }
    }
}
