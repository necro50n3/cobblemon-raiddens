package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.runner.ShowdownService;

public interface ShowdownEvent {
    String build(PokemonBattle battle);

    default void send(PokemonBattle battle) {
        String[] message = {this.build(battle)};
        ShowdownService.Companion.getService().send(battle.getBattleId(), message);
    }
}
