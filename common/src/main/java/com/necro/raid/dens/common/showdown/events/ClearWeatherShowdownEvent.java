package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class ClearWeatherShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval battle.field.weather = ''; battle.field.weatherState = { id: '' };";
    }
}
