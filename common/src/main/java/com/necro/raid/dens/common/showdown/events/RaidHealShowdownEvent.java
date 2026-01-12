package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record RaidHealShowdownEvent(float heal) implements ShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "battle.heal(Math.floor(p.maxhp * %1$f), p); " +
                "} ",
            this.heal
        );
    }
}
