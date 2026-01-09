package com.necro.raid.dens.common.mixins.ai;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.necro.raid.dens.common.data.raid.RaidAI;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(RandomBattleAI.class)
public abstract class RandomBattleAIMixin {
    @Inject(method = "choose", at = @At("HEAD"), remap = false, cancellable = true)
    private void chooseInject(ActiveBattlePokemon pokemon, PokemonBattle battle, BattleSide aiSide, ShowdownMoveset moveset, boolean forceSwitch, CallbackInfoReturnable<ShowdownActionResponse> cir) {
        if (!((IRaidBattle) pokemon.getBattle()).crd_isRaidBattle()) return;
        else if (moveset == null) {
            cir.setReturnValue(PassActionResponse.INSTANCE);
            return;
        }

        List<InBattleMove> filteredMoves = moveset.getMoves().stream()
            .filter(InBattleMove::canBeUsed)
            .filter(move -> {
                if (move.mustBeUsed()) return true;
                List<Targetable> target = move.getTarget().getTargetList().invoke(pokemon);
                return target == null || !target.isEmpty();
            })
            .filter(move -> !RaidAI.BLOCKED_MOVES.contains(move.id))
            .collect(Collectors.toCollection(ArrayList::new));
        if (filteredMoves.isEmpty()) {
            cir.setReturnValue(PassActionResponse.INSTANCE);
            return;
        }

        Collections.shuffle(filteredMoves);
        InBattleMove bestMove = filteredMoves.getFirst();

        List<Targetable> target = bestMove.mustBeUsed() ? null : bestMove.getTarget().getTargetList().invoke(pokemon);
        if (target == null || target.isEmpty()) {
            cir.setReturnValue(new MoveActionResponse(bestMove.id, null, null));
        }
        else {
            Collections.shuffle(target);
            ActiveBattlePokemon chosenTarget = (ActiveBattlePokemon) target.getFirst();
            cir.setReturnValue(new MoveActionResponse(bestMove.id, chosenTarget.getPNX(), null));
        }
    }
}
