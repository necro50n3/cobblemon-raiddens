package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record SetBoostShowdownEvent(Stat stat, int stages, int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "for (let p of battle.sides[%3$d].pokemon) { " +
                    "if (!p) continue; " +
                    "p.boosts['%1$s'] = %2$d; " +
                "} ",
            this.stat.getShowdownId(), this.stages, this.targetSide - 1
        );
    }
}
