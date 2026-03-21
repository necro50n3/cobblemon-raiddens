package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

import java.util.Map;

public record PlayerMapBoostShowdownEvent(Map<Stat, Integer> stats) implements BroadcastingShowdownEvent {
    @Override
    public void send(PokemonBattle battle) {
        this.stats.forEach((stat, stages) -> new PlayerBoostShowdownEvent(stat, stages).send(battle));
    }

    public String build(PokemonBattle battle) {
        return "";
    }
}
