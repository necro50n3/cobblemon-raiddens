package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record ClearSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "const condition = battle.dex.conditions.get('%1$s'); " +
                "const side = battle.sides[%2$d]; " +
                "if (condition && side.sideConditions[condition.id]) delete side.sideConditions[condition.id]; ",
            this.sideCondition, this.side - 1
        );
    }
}
