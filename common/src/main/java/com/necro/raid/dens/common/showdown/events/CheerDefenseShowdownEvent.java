package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record CheerDefenseShowdownEvent(String origin) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "battle.add('cheer', 'cheer_defense', '%1$s'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "p.addVolatile('cheerdefense'); " +
                "} ",
            this.origin
        );
    }
}
