package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class MegaEvolveShowdownEvent implements BroadcastingShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return ">eval " +
            "for (let p of battle.sides[1].pokemon) { " +
                "if (!p) continue; " +
                "if (p.species.baseSpecies === 'Zygarde') p.canMegaEvo = 'Zygarde-Mega'; " +
                "battle.actions.runMegaEvo(p); " +
            "} ";
    }
}
