package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;

public record StartRaidShowdownEvent(RaidBattleState battleState) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        Builder builder = new Builder();
        // TODO: Implement battle state mappings
        return builder.build();
    }

    private static class Builder {
        private String string;

        private Builder() {
            this.string = ">eval battle.sides[1].pokemon[0].addVolatile('raidboss'); ";
        }

        public String build() {
            return string;
        }
    }
}
