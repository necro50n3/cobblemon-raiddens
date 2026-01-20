package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class ResetBossShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "battle.add('raidenergy', '%1$s', true); " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "for (let i in p.boosts) { " +
                        "if (p.boosts[i] >= 0) continue; " +
                        "p.boosts[i] = 0; " +
                    "} " +
                    "if (p.status !== 'shield') p.cureStatus(); " +
                    "battle.add('clearboss', p, '%1$s'); " +
                "}",
            battle.getSide2().getActors()[0].getUuid()
        );
    }
}
