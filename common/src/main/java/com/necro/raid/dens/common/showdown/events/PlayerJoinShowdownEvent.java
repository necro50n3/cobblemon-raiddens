package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record PlayerJoinShowdownEvent(String player) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(">eval battle.add('playerjoin', battle.sides[1].pokemon[0], '%1$s');", this.player);
    }
}
