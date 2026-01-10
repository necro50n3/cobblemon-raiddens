package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record StatBoostShowdownEvent(Stat stat, int stages, int targetSide, boolean isSilent) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        String raidMessage = this.isSilent ? "" : "battle.add('raidenergy', '%3$s'); ";
        return String.format(
            ">eval " +
                "var boosts = {};" +
                "boosts['%1$s'] = %2$d; " +
                raidMessage +
                "for (let p of battle.sides[%4$d].pokemon) { " +
                    "if (!p) continue; " +
                    "var boost = battle.runEvent('ChangeBoost', p, null, null, {...boosts}); " +
                    "boost = p.getCappedBoost(boost); " +
                    "boost = battle.runEvent('TryBoost', p, null, null, {...boost}); " +
                    "for (let boostName in boost) { " +
                        "const currentBoost = { [boostName]: boost[boostName], }; " +
                        "let boostBy = p.boostBy(currentBoost); " +
                        "if (boostBy) battle.runEvent('AfterEachBoost', p, null, null, currentBoost); " +
                    "} " +
                    "console.log(JSON.stringify(p.boosts)); " +
                "} ",
            this.stat.getShowdownId(), this.stages, battle.getSide2().getActors()[0].getUuid(), this.targetSide - 1
        );
    }
}
