package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

public record SetSideConditionShowdownEvent(String sideCondition, int side) implements ShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "const condition = battle.dex.conditions.get('%1$s'); " +
                "const side = battle.sides[%2$d]; " +
                "const pokemon = side.pokemon[0]; " +
                "if (condition && !side.sideConditions[condition.id]) { " +
                    "side.sideConditions[condition.id] = { " +
                        "id: condition.id, " +
                        "target: side, " +
                        "source: pokemon, " +
                        "sourceSlot: pokemon.getSlot(), " +
                        "duration: condition.duration, " +
                    "}; " +
                    "battle.effectState.layers = 1; " +
                "} " +
                "else if (condition && side.sideConditions[condition.id]) { " +
                    "if (condition.id === 'spikes' && battle.effectState.layers < 3) battle.effectState.layers++; " +
                    "if (condition.id === 'toxicspikes' && battle.effectState.layers < 2) battle.effectState.layers++; " +
                "} ",
            this.sideCondition, this.side - 1
        );
    }
}
