package com.necro.raid.dens.common.raids.battle;

import com.necro.raid.dens.common.showdown.events.*;

import java.util.Optional;
import java.util.Set;

public class RaidBattleState {
    public final TrainerSide trainerSide;
    public final BossSide bossSide;
    private String terrain;
    private String weather;

    public RaidBattleState() {
        this.trainerSide = new TrainerSide();
        this.bossSide = new BossSide();
        this.terrain = null;
        this.weather = null;
    }

    public Optional<ShowdownEvent> addTerrain(String terrain) {
        if (this.terrain != null && this.terrain.equals(terrain)) return Optional.empty();
        this.terrain = terrain;
        return Optional.of(new SetTerrainShowdownEvent(terrain, true));
    }

    public Optional<ShowdownEvent> removeTerrain() {
        if (this.terrain == null) return Optional.empty();
        this.terrain = null;
        return Optional.of(new ClearTerrainShowdownEvent());
    }

    public Optional<ShowdownEvent> addWeather(String weather) {
        if (this.weather != null && this.weather.equals(weather)) return Optional.empty();
        this.weather = weather;
        return Optional.of(new SetWeatherShowdownEvent(weather, true));
    }

    public Optional<ShowdownEvent> removeWeather() {
        if (this.weather == null) return Optional.empty();
        this.weather = null;
        return Optional.of(new ClearWeatherShowdownEvent());
    }

    public Optional<ShowdownEvent> swapSideConditions() {
        if (this.trainerSide.sideConditions.isEmpty() && this.bossSide.sideConditions.isEmpty()) return Optional.empty();
        Set<String> tempSideConditions = this.trainerSide.sideConditions;
        this.trainerSide.sideConditions = this.bossSide.sideConditions;
        this.bossSide.sideConditions = tempSideConditions;
        return Optional.of(new SwapSideConditionsShowdownEvent());
    }
}
