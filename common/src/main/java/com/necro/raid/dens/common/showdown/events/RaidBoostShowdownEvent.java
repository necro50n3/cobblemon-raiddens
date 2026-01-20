package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public record RaidBoostShowdownEvent(Stat stat, int stages) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        return String.format(
            ">eval " +
                "battle.add('raidenergy', '%3$s', true); " +
                "var boosts = {};" +
                "boosts['%1$s'] = %2$d; " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "var boost = battle.runEvent('ChangeBoost', p, null, null, {...boosts}); " +
                    "boost = p.getCappedBoost(boost); " +
                    "boost = battle.runEvent('TryBoost', p, null, null, {...boost}); " +
                    "for (let boostName in boost) { " +
                        "const currentBoost = { [boostName]: boost[boostName], }; " +
                        "let boostBy = p.boostBy(currentBoost); " +
                        "if (boostBy) battle.runEvent('AfterEachBoost', p, null, null, currentBoost); " +
                        "let msg = '-raidboost'; " +
                        "if (boost[boostName] < 0 || p.boosts[boostName] === -6) { " +
                            "msg = '-raidunboost'; " +
                            "boostBy = -boostBy; " +
                        "} " +
                        "if (boostBy) { " +
                            "battle.add(msg, p, boostName, boostBy); " +
                            "battle.runEvent('AfterEachBoost', p, null, null, currentBoost); " +
                        "} " +
                    "} " +
                "} ",
            this.stat.getShowdownId(), this.stages, battle.getSide2().getActors()[0].getUuid()
        );
    }
}
