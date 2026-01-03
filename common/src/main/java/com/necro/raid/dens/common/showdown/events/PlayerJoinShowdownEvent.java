package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record PlayerJoinShowdownEvent(float currentHealth, String player) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "let p = battle.sides[1].pokemon[0]; " +
                "p.hp = %1$f; " +
                "battle.add('playerjoin', p, '%2$s');",
            this.currentHealth, this.player
        );
    }
}
