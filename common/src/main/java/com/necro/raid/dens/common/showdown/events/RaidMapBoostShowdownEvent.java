package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

import java.util.Map;

public record RaidMapBoostShowdownEvent(Map<Stat, Integer> stats) implements ShowdownEvent {
    @Override
    public void send(PokemonBattle battle) {
        this.stats.forEach((stat, stages) -> new RaidBoostShowdownEvent(stat, stages).send(battle));
    }

    public String build(PokemonBattle battle) {
        return "";
    }
}
