package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record ClearNegativeBoostShowdownEvent(int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "for (let p of battle.sides[%1$d].pokemon) { " +
                    "if (!p) continue; " +
                    "for (let i in p.boosts) { " +
                        "if (p.boosts[i] >= 0) continue; " +
                        "p.boosts[i] = 0; " +
                    "} " +
                "}",
            this.targetSide - 1
        );
    }
}
