package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record CheerAttackShowdownEvent(String origin) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "battle.add('cheer', 'cheer_attack', '%1$s'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "p.addVolatile('cheerattack'); " +
                "} ",
            this.origin
        );
    }
}
