package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record ClearBoostShowdownEvent(int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "for (let p of battle.sides[%1$d].pokemon) { " +
                    "if (!p) continue; " +
                    "p.clearBoosts(); " +
                "} ",
            this.targetSide - 1
        );
    }
}
