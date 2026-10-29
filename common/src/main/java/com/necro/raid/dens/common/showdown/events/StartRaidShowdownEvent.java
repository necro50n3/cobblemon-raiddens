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

public record StartRaidShowdownEvent(RaidBattleState battleState, boolean isDynamax, boolean isTera) implements ShowdownEvent {
    @SuppressWarnings("deprecation")
    public String build(PokemonBattle battle) {
        Builder builder = new Builder(this.isDynamax, this.isTera);
        List<Pair<ContextManager, BattleContext>> contexts = new ArrayList<>();

        if (this.battleState.terrain != null) {
            builder.addTerrain(this.battleState.terrain);
            contexts.add(new Pair<>(battle.getContextManager(), new BasicContext(this.battleState.terrain, battle.getTurn(), BattleContext.Type.TERRAIN, null)));
        }
        this.battleState.fields.forEach(field -> {
            builder.addField(field);
            contexts.add(new Pair<>(battle.getContextManager(), new BasicContext(field, battle.getTurn(), BattleContext.Type.ROOM, null)));
        });
        if (this.battleState.weather != null) {
            builder.addWeather(this.battleState.weather);
            contexts.add(new Pair<>(battle.getContextManager(), new BasicContext(this.battleState.weather, battle.getTurn(), BattleContext.Type.WEATHER, null)));
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

        builder.build().forEach(event -> event.send(battle));
        return "";
    }

    @Override
    public void send(PokemonBattle battle) {
        this.build(battle);
    }

    private static class Builder {
        private final List<ShowdownEvent> events;

        private Builder(boolean isDynamax, boolean isTera) {
            this.events = new ArrayList<>();
            this.events.add(new ShowdownEvents.RaidStartShowdownEvent());
            if (isDynamax) this.events.add(new ShowdownEvents.DynamaxShowdownEvent(false));
            if (isTera) this.events.add(new ShowdownEvents.TerastallizeShowdownEvent(false));
        }

        private void addTerrain(String terrain) {
            this.events.add(new ShowdownEvents.SetTerrainShowdownEvent(terrain, true));
        }

        private void addField(String field) {
            this.events.add(new ShowdownEvents.SetFieldShowdownEvent(field));
        }

        private void addWeather(String weather) {
            this.events.add(new ShowdownEvents.SetWeatherShowdownEvent(weather, true));
        }

        private void addSideConditions(int side, String sideCondition) {
            this.events.add(new ShowdownEvents.SetSideConditionShowdownEvent(sideCondition, side - 1));
        }

        private void addVolatile(VolatileStatus status) {
            this.events.add(new ShowdownEvents.SetStatusShowdownEvent(status, 1));
        }

        private void setBoost(Stat stat, int stages) {
            this.events.add(new ShowdownEvents.SetBoostShowdownEvent(stat, stages, 1));
        }

        private List<ShowdownEvent> build() {
            return this.events;
        }
    }
}
