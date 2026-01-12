package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;

public record CureStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        if (this.status instanceof VolatileStatus) {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[%2$d].pokemon) { " +
                        "if (!p) continue;" +
                        "p.removeVolatile('%1$s'); " +
                    "} ",
                this.status.getShowdownName(), this.targetSide - 1);
        }
        else {
            return String.format(
                ">eval " +
                    "for (let p of battle.sides[%1$d].pokemon) { " +
                        "if (!p) continue; " +
                        "if (p.status !== 'shield') p.clearStatus(); " +
                    "}",
                this.targetSide - 1);
        }
    }
}
