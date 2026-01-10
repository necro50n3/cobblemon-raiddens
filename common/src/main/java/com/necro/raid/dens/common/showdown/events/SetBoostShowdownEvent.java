package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record SetBoostShowdownEvent(Stat stat, int stages, int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "var boosts = {};" +
                "boosts['%1$s'] = %2$d; " +
                "for (let p of battle.sides[%4$d].pokemon) { " +
                    "if (!p) continue; " +
                    "p.setBoost(boosts); " +
                "} ",
            this.stat.getShowdownId(), this.stages, battle.getSide2().getActors()[0].getUuid(), this.targetSide - 1
        );
    }
}
