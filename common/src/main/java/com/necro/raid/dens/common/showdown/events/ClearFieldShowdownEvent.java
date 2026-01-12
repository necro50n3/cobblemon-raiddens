package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record ClearFieldShowdownEvent(String field) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "const pseudoWeather = battle.dex.conditions.get('%1$s'); " +
                "if (battle.field.pseudoWeather[pseudoWeather.id]) delete battle.field.pseudoWeather[pseudoWeather.id]; ",
            this.field
        );
    }
}
