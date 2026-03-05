package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class DoNothingShowdownEvent implements ShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return ">eval battle.add('donothing');";
    }
}
