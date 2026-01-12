package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class DragonCheerShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval " +
            "const status = battle.dex.conditions.get('dragoncheer'); " +
            "for (let p of battle.sides[0].pokemon) { " +
                "if (!p) continue; " +
                "else if (p.volatiles['focusenergy']) continue; " +
                "else if (status && !p.volatiles[status.id]) { " +
                    "p.volatiles[status.id] = { id: status.id, name: status.name, target: p }; " +
                "} " +
                "battle.effectState.hasDragonType = p.hasType('Dragon'); " +
                "battle.add('-start', p, 'move: Dragon Cheer'); " +
            "} ";
    }
}
