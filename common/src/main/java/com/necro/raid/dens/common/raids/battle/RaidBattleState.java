package com.necro.raid.dens.common.raids.battle;


import com.necro.raid.dens.common.showdown.events.ShowdownEvent;
import com.necro.raid.dens.common.showdown.events.ShowdownEvents;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RaidBattleState {
    public final TrainerSide trainerSide;
    public final BossSide bossSide;
    public String terrain;
    public Set<String> fields;
    public String weather;

    public RaidBattleState() {
        this.trainerSide = new TrainerSide();
        this.bossSide = new BossSide();
        this.terrain = null;
        this.fields = new HashSet<>();
        this.weather = null;
    }

    public Optional<ShowdownEvent> addTerrain(String terrain) {
        if (this.terrain != null && this.terrain.equals(terrain)) return Optional.empty();
        this.terrain = terrain;
        return Optional.of(new ShowdownEvents.SetTerrainShowdownEvent(terrain, true));
    }

    public Optional<ShowdownEvent> removeTerrain() {
        if (this.terrain == null) return Optional.empty();
        this.terrain = null;
        return Optional.of(new ShowdownEvents.ClearTerrainShowdownEvent());
    }

    public Optional<ShowdownEvent> addField(String field) {
        if (this.fields.contains(field)) return this.removeField(field);
        this.fields.add(field);
        return Optional.of(new ShowdownEvents.SetFieldShowdownEvent(field));
    }

    public Optional<ShowdownEvent> removeField(String field) {
        if (!this.fields.contains(field)) return Optional.empty();
        this.fields.remove(field);
        return Optional.of(new ShowdownEvents.ClearFieldShowdownEvent(field));
    }

    public Optional<ShowdownEvent> addWeather(String weather) {
        if (this.weather != null && this.weather.equals(weather)) return Optional.empty();
        else if (this.weather != null && RaidConditions.PRIMAL_WEATHER.contains(this.weather) && !RaidConditions.PRIMAL_WEATHER.contains(weather)) {
            return Optional.empty();
        }
        this.weather = weather;
        return Optional.of(new ShowdownEvents.SetWeatherShowdownEvent(weather, true));
    }

    public Optional<ShowdownEvent> removeWeather() {
        if (this.weather == null) return Optional.empty();
        this.weather = null;
        return Optional.of(new ShowdownEvents.ClearWeatherShowdownEvent());
    }

    public Optional<ShowdownEvent> swapSideConditions() {
        if (this.trainerSide.sideConditions.isEmpty() && this.bossSide.sideConditions.isEmpty()) return Optional.empty();
        Set<String> tempSideConditions = this.trainerSide.sideConditions;
        this.trainerSide.sideConditions = this.bossSide.sideConditions;
        this.bossSide.sideConditions = tempSideConditions;
        return Optional.of(new ShowdownEvents.SwapSideConditionsShowdownEvent());
    }
}
