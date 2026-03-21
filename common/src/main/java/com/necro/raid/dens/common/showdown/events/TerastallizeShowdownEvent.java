package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;

public class TerastallizeShowdownEvent implements BroadcastingShowdownEvent {
    @Override
    public String build(PokemonBattle battle) {
        if (ModCompat.MEGA_SHOWDOWN.isLoaded()) {
            battle.dispatch(() -> {
                battle.getSide2().getActivePokemon().forEach(active -> {
                    BattlePokemon battlePokemon = active.getBattlePokemon();
                    if (battlePokemon == null || battlePokemon.getEntity() == null) return;
                    RaidDensMSDCompat.setupTera(battlePokemon.getEntity(), battlePokemon.getEffectedPokemon());
                });
                return DispatchResultKt.getGO();
            });
        }
        return ">eval " +
            "for (let p of battle.sides[1].pokemon) { " +
                "if (!p) continue; " +
                "battle.actions.terastallize(p); " +
            "} ";
    }
}
