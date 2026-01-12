package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record ClearBoostShowdownEvent(int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "for (let p of battle.sides[%1$d].pokemon) { " +
                    "if (!p) continue; " +
                    "for (let boost in p.boosts) { " +
                        "p.boosts[boost] = 0; " +
                    "} " +
                "} ",
            this.targetSide - 1
        );
    }
}
