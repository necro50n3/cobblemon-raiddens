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
                        "if (!status || status.id === 'typechange') continue; " +
                        "if (p.volatiles[status.id]) { " +
                            "if (status.onRestart) battle.singleEvent('Restart', status, p.volatiles[status.id], p, null, null); " +
                            "continue;" +
                        "} " +
                        "p.volatiles[status.id] = { id: status.id, name: status.name, target: p }; " +
                        "if (status.id === 'confusion') p.volatiles[status.id].time = battle.random(2, 6); " +
                    "} ",
                this.status.getShowdownName(), this.targetSide - 1);
        }
        else {
            return String.format(
                ">eval " +
                    "const status = battle.dex.conditions.get('%1$s'); " +
                    "for (let p of battle.sides[%2$d].pokemon) { " +
                        "if (status && p.status !== 'shield') {" +
                            "if (p.status === status.id) continue; " +
                            "battle.runEvent('SetStatus', p, null, null, status); " +
                            "p.status = status.id; " +
                            "p.statusState = { id: status.id, target: p };" +
                            "if (status.duration) p.statusState.duration = status.duration;" +
                            "if (status.durationCallback) p.statusState.duration = status.durationCallback.call(battle, p, null, null);" +
                            "if (status.id === 'slp') {" +
                                "p.statusState.startTime = battle.random(2, 5); " +
                                "p.statusState.time = p.statusState.startTime; " +
                                "battle.effectState = p.statusState; " +
                            "} " +
                            "battle.runEvent('AfterSetStatus', p, null, null, status);" +
                        "} " +
                    "}",
                this.status.getShowdownName(), this.targetSide - 1);
        }
    }
}
