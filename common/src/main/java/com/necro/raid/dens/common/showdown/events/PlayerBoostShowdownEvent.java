package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record PlayerBoostShowdownEvent(Stat stat, int stages) implements BroadcastingShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "var boosts = {};" +
                "boosts['%1$s'] = %2$d; " +
                "battle.add('raidenergy', '%3$s'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "battle.boost(boosts, p, null, '[from] Raid'); " +
                "} ",
            this.stat.getShowdownId(), this.stages, battle.getSide2().getActors()[0].getUuid()
        );
    }
}
