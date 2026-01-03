package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record CheerAttackShowdownEvent(int stages, String origin) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "var boosts = {'atk': %1$d, 'spa': %1$d};" +
                "battle.add('cheer', 'cheer_attack', '%2$s'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "battle.boost(boosts, p, null, { effectType: 'BagItem', name: 'cheer_attack' }); " +
                "} ",
            this.stages, this.origin
        );
    }
}
