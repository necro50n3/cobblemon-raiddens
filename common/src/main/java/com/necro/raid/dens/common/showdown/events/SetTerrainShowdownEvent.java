package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record SetTerrainShowdownEvent(String terrain, boolean isSilent) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        if (this.isSilent) {
            return String.format(
                ">eval " +
                    "const pokemon = battle.sides[1].pokemon[0]; " +
                    "const terrain = this.battle.dex.conditions.get('%1$s'); " +
                    "battle.field.terrain = terrain.id; " +
                    "battle.field.terrainState = { " +
                        "id: terrain.id, " +
                        "source: pokemon, " +
                        "sourceSlot: pokemon.getSlot(), " +
                        "duration: terrain.duration " +
                    "};",
                this.terrain
            );
        }
        else {
            return String.format(
                ">eval " +
                    "const pokemon = battle.sides[1].pokemon[0]; " +
                    "battle.field.setTerrain('%s', pokemon); ",
                this.terrain
            );
        }
    }
}
