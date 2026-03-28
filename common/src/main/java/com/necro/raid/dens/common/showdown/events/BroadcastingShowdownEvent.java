package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

import java.util.Collection;

public interface BroadcastingShowdownEvent extends ShowdownEvent {
    @Override
    default void execute(RaidContext context) {
        broadcast(context.battles());
    }

    default void broadcast(Collection<PokemonBattle> battles) {
        for (PokemonBattle battle : battles) {
            this.send(battle);
        }
    }
}
