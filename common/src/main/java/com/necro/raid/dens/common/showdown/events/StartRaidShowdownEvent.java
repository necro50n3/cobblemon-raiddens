package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class StartRaidShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval battle.sides[1].pokemon[0].addVolatile('raidboss');";
    }
}
