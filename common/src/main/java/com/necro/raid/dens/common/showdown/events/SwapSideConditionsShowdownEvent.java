package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public class SwapSideConditionsShowdownEvent implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return ">eval " +
            "const sourceSideConditions = battle.sides[0].sideConditions; " +
            "const targetSideConditions = battle.sides[1].sideConditions; " +
            "const sourceTemp: typeof sourceSideConditions = {}; " +
            "const targetTemp: typeof targetSideConditions = {}; " +
            "for (const id in sourceSideConditions) { " +
                "if (!sideConditions.includes(id)) continue; " +
                "sourceTemp[id] = sourceSideConditions[id]; " +
                "delete sourceSideConditions[id]; " +
            "} " +
            "for (const id in targetSideConditions) { " +
                "if (!sideConditions.includes(id)) continue; " +
                "targetTemp[id] = targetSideConditions[id]; " +
                "delete targetSideConditions[id]; " +
            "} " +
            "for (const id in sourceTemp) { " +
                "targetSideConditions[id] = sourceTemp[id]; " +
            "} " +
            "for (const id in targetTemp) { " +
                "sourceSideConditions[id] = targetTemp[id]; " +
            "}";
    }
}
