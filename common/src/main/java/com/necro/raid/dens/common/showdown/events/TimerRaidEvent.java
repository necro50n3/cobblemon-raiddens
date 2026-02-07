package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record TimerRaidEvent(int time) implements ShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return "";
    }

    @Override
    public void send(PokemonBattle battle) {}
}
