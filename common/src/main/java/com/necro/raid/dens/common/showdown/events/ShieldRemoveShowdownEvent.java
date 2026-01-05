package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class ShieldRemoveShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval " +
            "const p = battle.sides[1].pokemon[0]; " +
            "p.cureStatus(); " +
            "battle.add('shieldremove', p);";
    }
}
