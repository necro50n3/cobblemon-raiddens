package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class RaidCureShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval " +
            "for (let p of battle.sides[0].pokemon) { " +
                "if (!p) continue; " +
                "p.cureStatus(); " +
            "}";
    }
}
