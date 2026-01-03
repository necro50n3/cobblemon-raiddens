package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record SetTerrainShowdownEvent(String terrain) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "const pokemon = battle.sides[1].pokemon[0]; " +
                "battle.field.setTerrain('%s', pokemon); ",
            this.terrain
        );
    }
}
