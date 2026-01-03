package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record CheerDefenseShowdownEvent(int stages, String origin) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "var boosts = {'def': %1$d, 'spd': %1$d};" +
                "battle.add('cheer', 'cheer_defense', '%2$s'); " +
                "for (let p of battle.sides[0].pokemon) { " +
                    "if (!p) continue; " +
                    "battle.boost(boosts, p, null, { effectType: 'BagItem', name: 'cheer_defense' }); " +
                "} ",
            this.stages, this.origin
        );
    }
}
