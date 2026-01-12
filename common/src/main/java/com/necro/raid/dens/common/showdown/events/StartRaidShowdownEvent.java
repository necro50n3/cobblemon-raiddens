package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.ContextManager;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;
import com.mojang.datafixers.util.Pair;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.raids.battle.RaidConditions;

import java.util.ArrayList;
import java.util.List;

public record StartRaidShowdownEvent(RaidBattleState battleState) implements ShowdownEvent {
    @SuppressWarnings("deprecation")
    public String build(PokemonBattle battle) {
        Builder builder = new Builder();
        List<Pair<ContextManager, BattleContext>> contexts = new ArrayList<>();

        if (this.battleState.weather != null) {
            builder.addWeather(this.battleState.weather);
            contexts.add(new Pair<>(battle.getContextManager(), new BasicContext(this.battleState.weather, battle.getTurn(), BattleContext.Type.WEATHER, null)));
        }
        if (this.battleState.terrain != null) {
            builder.addTerrain(this.battleState.terrain);
            contexts.add(new Pair<>(battle.getContextManager(), new BasicContext(this.battleState.terrain, battle.getTurn(), BattleContext.Type.TERRAIN, null)));
        }
        this.battleState.trainerSide.sideConditions.forEach(sideCondition -> {
            builder.addSideConditions(this.battleState.trainerSide.side, sideCondition);
            BattleContext.Type type;
            if (RaidConditions.SCREENS.contains(sideCondition)) type = BattleContext.Type.SCREEN;
            else if (RaidConditions.HAZARDS.contains(sideCondition)) type = BattleContext.Type.HAZARD;
            else if (RaidConditions.TAILWIND.contains(sideCondition)) type = BattleContext.Type.TAILWIND;
            else type = BattleContext.Type.MISC;
            contexts.add(new Pair<>(battle.getSide1().getContextManager(), new BasicContext(sideCondition, battle.getTurn(), type, null)));
        });
        this.battleState.bossSide.sideConditions.forEach(sideCondition -> {
            builder.addSideConditions(this.battleState.bossSide.side, sideCondition);
            BattleContext.Type type;
            if (RaidConditions.SCREENS.contains(sideCondition)) type = BattleContext.Type.SCREEN;
            else if (RaidConditions.HAZARDS.contains(sideCondition)) type = BattleContext.Type.HAZARD;
            else if (RaidConditions.TAILWIND.contains(sideCondition)) type = BattleContext.Type.TAILWIND;
            else type = BattleContext.Type.MISC;
            contexts.add(new Pair<>(battle.getSide2().getContextManager(), new BasicContext(sideCondition, battle.getTurn(), type, null)));
        });
        this.battleState.bossSide.pokemon.volatileStatus.forEach(status -> {
            builder.addVolatile(status);
            contexts.add(new Pair<>(battle.getSide2().getContextManager(), new BasicContext(status.getShowdownName(), battle.getTurn(), BattleContext.Type.VOLATILE, null)));
        });
        this.battleState.bossSide.pokemon.boosts.forEach((stat, stages) -> {
            builder.setBoost(stat, stages);
            BattlePokemon pokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
            if (pokemon == null) return;
            contexts.add(new Pair<>(pokemon.getContextManager(), new BasicContext(stat.getShowdownId(), battle.getTurn(), BattleContext.Type.BOOST, null)));
        });

        battle.dispatch(() -> {
            contexts.forEach(pair -> pair.getFirst().add(pair.getSecond()));
            return DispatchResultKt.getGO();
        });

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
