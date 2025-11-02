package com.necro.raid.dens.common.mixins.ai;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(RandomBattleAI.class)
public abstract class RandomBattleAIMixin {
    @Inject(method = "choose", at = @At("HEAD"), remap = false, cancellable = true)
    private void chooseInject(ActiveBattlePokemon pokemon, PokemonBattle battle, BattleSide aiSide, ShowdownMoveset moveset, boolean forceSwitch, CallbackInfoReturnable<ShowdownActionResponse> cir) {
        if (!((IRaidBattle) pokemon.getBattle()).isRaidBattle()) return;
        else if (moveset == null) {
            cir.setReturnValue(PassActionResponse.INSTANCE);
            return;
        }

        List<InBattleMove> filteredMoves = new ArrayList<>(moveset.getMoves().stream()
            .filter(InBattleMove::canBeUsed)
            .filter(move -> move.mustBeUsed() || (move.getTarget().getTargetList().invoke(pokemon) != null && !move.getTarget().getTargetList().invoke(pokemon).isEmpty()))
            .filter(move -> !move.id.equals("lastresort"))
            .toList());
        if (filteredMoves.isEmpty()) cir.setReturnValue(new MoveActionResponse("struggle", null, null));

        Collections.shuffle(filteredMoves);
        InBattleMove bestMove = filteredMoves.getFirst();

        List<Targetable> target = bestMove.mustBeUsed() ? null
            : new ArrayList<>(bestMove.getTarget().getTargetList().invoke(pokemon).stream().filter(t -> !t.isAllied(pokemon)).toList());
        if (target == null) {
            cir.setReturnValue(new MoveActionResponse(bestMove.id, null, null));
        }
        else {
            Collections.shuffle(target);
            ActiveBattlePokemon chosenTarget = (ActiveBattlePokemon) target.getFirst();
            cir.setReturnValue(new MoveActionResponse(bestMove.id, chosenTarget.getPNX(), null));
        }
    }
}
