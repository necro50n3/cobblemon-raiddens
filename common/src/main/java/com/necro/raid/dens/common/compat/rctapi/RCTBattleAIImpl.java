package com.necro.raid.dens.common.compat.rctapi;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.mojang.datafixers.util.Function3;
import com.necro.raid.dens.common.raids.RaidAI;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

public class RCTBattleAIImpl {
    public static void choose(ActiveBattlePokemon pokemon, ShowdownMoveset moveset,
                              Function3<BattlePokemon, BattlePokemon, InBattleMove, Double> eval,
                              CallbackInfoReturnable<ShowdownActionResponse> cir
    ) {
        if (!((IRaidBattle) pokemon.getBattle()).isRaidBattle()) return;
        else if (moveset == null) {
            cir.setReturnValue(PassActionResponse.INSTANCE);
            return;
        }

        PokemonBattle battle = pokemon.getBattle();
        BattleActor p1Actor = battle.getSide1().getActors()[0];
        BattleActor p2Actor = battle.getSide2().getActors()[0];

        BattlePokemon playerPokemon = p1Actor.getActivePokemon().getFirst().getBattlePokemon();
        BattlePokemon npcPokemon = p2Actor.getActivePokemon().getFirst().getBattlePokemon();

        Map<InBattleMove, Double> scores = new HashMap<>();
        moveset.getMoves().forEach(move ->
            scores.put(move, Math.max(eval.apply(npcPokemon, playerPokemon, move), eval.apply(npcPokemon, npcPokemon, move)))
        );
        List<Map.Entry<InBattleMove, Double>> list = new ArrayList<>(scores.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        InBattleMove bestMove = list.getFirst().getKey();

        List<Targetable> target = bestMove.mustBeUsed() ? null
            : bestMove.getTarget().getTargetList().invoke(pokemon);
        if (target == null) {
            cir.setReturnValue(new MoveActionResponse(bestMove.id, null, null));
        }
        else {
            target = new ArrayList<>(target.stream().filter(t -> !t.isAllied(pokemon)).toList());
            Collections.shuffle(target);
            ActiveBattlePokemon chosenTarget = (ActiveBattlePokemon) target.getFirst();
            cir.setReturnValue(new MoveActionResponse(bestMove.id, chosenTarget.getPNX(), null));
        }
    }

    public static void evalMove(InBattleMove move, CallbackInfoReturnable<Double> cir) {
        if (RaidAI.BLOCKED_MOVES.contains(move.id) || !move.canBeUsed()) cir.setReturnValue(-1.0);
    }
}
