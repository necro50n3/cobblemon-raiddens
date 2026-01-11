package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record SetSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        // TODO: Implement side condition setting
        return "";
    }
}
