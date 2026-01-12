package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import net.minecraft.network.chat.Component;

public record StartRaidShowdownEvent(RaidBattleState battleState) implements ShowdownEvent {
    public String build(PokemonBattle battle) {
        Builder builder = new Builder();
        if (this.battleState.weather != null) {
            builder.addWeather(this.battleState.weather);
            battle.broadcastChatMessage(LocalizationUtilsKt.battleLang(String.format("weather.%s.start", this.battleState.weather)));
        }
        if (this.battleState.terrain != null) {
            builder.addTerrain(this.battleState.terrain);
            battle.broadcastChatMessage(LocalizationUtilsKt.battleLang(String.format("fieldstart.%s", this.battleState.terrain), Component.literal("UNKNOWN")));
        }
        if (!this.battleState.trainerSide.sideConditions.isEmpty()) {
            this.battleState.trainerSide.sideConditions.forEach(sideCondition -> {
                builder.addSideConditions(this.battleState.trainerSide.side, sideCondition);
            });
        }
        if (!this.battleState.bossSide.sideConditions.isEmpty()) {
            this.battleState.bossSide.sideConditions.forEach(sideCondition -> {
                builder.addSideConditions(this.battleState.bossSide.side, sideCondition);
            });
        }
        if (!this.battleState.bossSide.pokemon.volatileStatus.isEmpty()) {
            this.battleState.bossSide.pokemon.volatileStatus.forEach(builder::addVolatile);
        }
        this.battleState.bossSide.pokemon.boosts.forEach(builder::setBoost);
        return builder.build();
    }

    private static class Builder {
        private String string;

        private Builder() {
            this.string = ">eval battle.sides[1].pokemon[0].addVolatile('raidboss'); ";
        }

        private void addWeather(String weather) {
            this.string += String.format(
                "const weather = this.battle.dex.conditions.get('%1$s'); " +
                "if (weather && battle.field.weather === '') { " +
                    "battle.field.weather = weather.id; " +
                    "battle.field.weatherState = { id: weather.id }; " +
                    "if (weather.duration) battle.field.weatherState.duration = weather.duration; " +
                "} ",
                weather
            );
        }

        private void addTerrain(String terrain) {
            this.string += String.format(
                "const pokemon = battle.sides[1].pokemon[0]; " +
                "const terrain = battle.dex.conditions.get('%1$s'); " +
                "if (terrain && battle.field.terrain === '') { " +
                    "battle.field.terrain = terrain.id; " +
                    "battle.field.terrainState = { " +
                        "id: terrain.id, " +
                        "source: pokemon, " +
                        "sourceSlot: pokemon.getSlot(), " +
                        "duration: terrain.duration " +
                    "}; " +
                "} ",
                terrain
            );
        }

        private void addSideConditions(int side, String sideCondition) {

        }

        private void addVolatile(VolatileStatus status) {
            this.string += String.format(
                "const status = battle.dex.conditions.get('%1$s'); " +
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "if (status) p.volatiles[status.id] = { id: status.id, name: status.name, target: p }; " +
                "} ",
                status.getShowdownName()
            );
        }

        private void setBoost(Stat stat, int stages) {
            this.string += String.format(
                "for (let p of battle.sides[1].pokemon) { " +
                    "if (!p) continue; " +
                    "p.boosts['%1$s'] = %2$d; " +
                "} ",
                stat.getShowdownId(), stages
            );
        }

        private String build() {
            return string;
        }
    }
}
