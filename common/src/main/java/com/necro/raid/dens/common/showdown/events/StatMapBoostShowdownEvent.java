package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

import java.util.Map;

public record StatMapBoostShowdownEvent(Map<Stat, Integer> stats, int targetSide, boolean isSilent) implements ShowdownEvent {
    @Override
    public void send(PokemonBattle battle) {
        this.stats.forEach((stat, stages) -> new StatBoostShowdownEvent(stat, stages, this.targetSide, this.isSilent).send(battle));
    }

    public String build(PokemonBattle battle) {
        return "";
    }
}
