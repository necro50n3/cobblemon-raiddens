package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record UseMoveShowdownEvent(String move, int target) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "let p = battle.sides[1].pokemon[0]; " +
                "p.side.lastSelectedMove = battle.toID('%1$s'); " +
                "battle.actions.runMove('%1$s', p, %2$d, null, null, true);",
            this.move, this.target
        );
    }
}
