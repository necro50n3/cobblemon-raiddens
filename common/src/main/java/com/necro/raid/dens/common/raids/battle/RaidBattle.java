package com.necro.raid.dens.common.raids.battle;

import com.necro.raid.dens.common.raids.battle.component.TerrainComponent;
import com.necro.raid.dens.common.raids.battle.component.WeatherComponent;

public class RaidBattle {
    public final RaidSide[] sides;
    private TerrainComponent terrain;
    private WeatherComponent weather;

    public RaidBattle() {
        this.sides = new RaidSide[2];
        this.terrain = null;
        this.weather = null;
    }

    public void addTerrain(TerrainComponent terrain) {
        if (this.terrain != null) return;
        this.terrain = terrain;
    }

    public void removeTerrain(TerrainComponent terrain) {
        if (this.terrain == null) return;
        this.terrain = null;
    }

    public void addWeather(WeatherComponent weather) {
        if (this.weather != null) return;
        this.weather = weather;
    }

    public void removeWeather(WeatherComponent weather) {
        if (this.weather == null) return;
        this.weather = null;
    }

    public static class Builder {
        private final RaidBattle battle;

        public Builder() {
            this.battle = new RaidBattle();
            this.battle.sides[0] = new RaidSide();
            this.battle.sides[1] = new RaidSide();
            this.battle.sides[0].pokemon[0] = new RaidPokemon();
            this.battle.sides[0].pokemon[1] = new RaidPokemon();
            this.battle.sides[1].pokemon[0] = new RaidPokemon();
            this.battle.sides[1].pokemon[1] = new RaidPokemon();
        }

        public RaidBattle build() {
            return this.battle;
        }
    }
}
