package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record SetFieldShowdownEvent(String field) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "const pokemon = battle.sides[1].pokemon[0]; " +
                "const pseudoWeather = battle.dex.conditions.get('%1$s'); " +
                "battle.field.pseudoWeather[pseudoWeather.id] = { " +
                    "id: pseudoWeather.id, " +
                    "source: pokemon, " +
                    "sourceSlot: pokemon.getSlot(), " +
                    "duration: pseudoWeather.duration " +
                "};",
            this.field
        );
    }
}
