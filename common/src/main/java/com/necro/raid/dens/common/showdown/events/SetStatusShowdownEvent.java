package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;

public record SetStatusShowdownEvent(Status status, int targetSide) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        if (this.status instanceof VolatileStatus) {
            return String.format(
                ">eval " +
                    "const status = battle.dex.conditions.get('%1$s'); " +
                    "for (let p of battle.sides[%2$d].pokemon) { " +
                        "if (status) p.volatiles[status.id] = { id: status.id, name: status.name, target: p };" +
                    "} ",
                this.status.getShowdownName(), this.targetSide - 1);
        }
        else {
            return String.format(
                ">eval " +
                    "const status = battle.dex.conditions.get('%1$s'); " +
                    "for (let p of battle.sides[%2$d].pokemon) { " +
                        "if (status && p.status !== 'shield') {" +
                            "p.status = status.id; " +
                            "p.statusState = { id: status.id, target: p };" +
                        "} " +
                    "}",
                this.status.getShowdownName(), this.targetSide - 1);
        }
    }
}
