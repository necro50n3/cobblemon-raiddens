package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.runner.ShowdownService;

public interface ShowdownEvent extends AbstractEvent {
    @Override
    default void execute(RaidContext context) {
        if (context.battle() == null) return;
        send(context.battle());
    }

    String build(PokemonBattle battle);

    default void send(PokemonBattle battle) {
        String[] message = {this.build(battle)};
        try { ShowdownService.Companion.getService().send(battle.getBattleId(), message); }
        catch (Exception ignored) {}
    }
}
