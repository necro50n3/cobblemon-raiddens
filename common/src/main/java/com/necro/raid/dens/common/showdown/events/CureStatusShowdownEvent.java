package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;

public record CureStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        if (this.status instanceof VolatileStatus) {
            return String.format(
                ">eval " +
                    "const p = battle.sides[%2$d].pokemon[0]; " +
                    "p.removeVolatile('%1$s');",
                this.status.getShowdownName(), this.targetSide - 1);
        }
        else {
            return String.format(
                ">eval " +
                    "const p = battle.sides[%1$d].pokemon[0]; " +
                    "if (p.status === 'shield') return; " +
                    "p.cureStatus();",
                this.targetSide
            );
        }
    }
}
