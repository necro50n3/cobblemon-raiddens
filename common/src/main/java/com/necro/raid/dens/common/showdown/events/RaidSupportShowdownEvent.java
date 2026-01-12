package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record RaidSupportShowdownEvent(String move, Stat stat, int stages) implements BroadcastingShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "var boosts = {};" +
                "boosts['%2$s'] = %3$d; " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "else if (['gearup', 'magneticflux'].includes('%1$s')) { " +
                        "if (!p.hasAbility('plus') && !p.hasAbility('minus')) continue; " +
                    "} " +
                    "battle.boost(boosts, p, null, '[from] Raid'); " +
                "} ",
            this.move, this.stat.getShowdownId(), this.stages
        );
    }
}
