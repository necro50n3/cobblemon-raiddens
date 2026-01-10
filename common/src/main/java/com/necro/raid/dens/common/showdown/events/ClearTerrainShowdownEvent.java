package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class ClearTerrainShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval battle.field.clearTerrain();";
    }
}
