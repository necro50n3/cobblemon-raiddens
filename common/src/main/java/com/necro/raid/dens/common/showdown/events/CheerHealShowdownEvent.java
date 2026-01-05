package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record CheerHealShowdownEvent(String origin) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "battle.add('cheer', 'cheer_heal', '%1$s'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "p.heal(Math.floor(p.maxhp * 0.5)); " +
                    "p.cureStatus(); " +
                    "battle.add('-heal', p, p.getHealth, '[from] bagitem: ' + 'cheer_heal'); " +
                "} ",
            this.origin
        );
    }
}
