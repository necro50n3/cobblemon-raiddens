package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record SetWeatherShowdownEvent(String weather, boolean isSilent) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        if (this.isSilent) {
            return String.format(
                ">eval " +
                    "const weather = this.battle.dex.conditions.get('%1$s'); " +
                    "battle.field.weather = weather.id; " +
                    "battle.field.weatherState = { id: weather.id };" +
                    "if (weather.duration) battle.field.weatherState.duration = weather.duration;",
                this.weather
            );
        }
        else {
            return String.format(
                ">eval " +
                    "const pokemon = battle.sides[1].pokemon[0]; " +
                    "battle.field.setWeather('%s', pokemon);",
                this.weather
            );
        }
    }
}
