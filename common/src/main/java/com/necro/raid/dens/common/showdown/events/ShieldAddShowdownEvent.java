package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class ShieldAddShowdownEvent implements BroadcastingShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval " +
            "for (let p of battle.sides[1].pokemon) { " +
                "if (!p) continue; " +
                "p.clearStatus(); " +
                "p.trySetStatus('shield', p); " +
                "battle.add('shieldadd', p); " +
            "} ";
    }
}
