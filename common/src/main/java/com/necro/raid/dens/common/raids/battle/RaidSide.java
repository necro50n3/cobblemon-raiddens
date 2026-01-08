package com.necro.raid.dens.common.raids.battle;

import com.necro.raid.dens.common.raids.battle.component.ScreensComponent;

import java.util.HashSet;
import java.util.Set;

public class RaidSide {
    public final RaidPokemon[] pokemon;
    private final Set<ScreensComponent> screens;

    public RaidSide() {
        this.pokemon = new RaidPokemon[2];
        this.screens = new HashSet<>();
    }

    public void addScreen(ScreensComponent screen) {
        if (this.screens.contains(screen)) return;
        this.screens.add(screen);
    }

    public void removeScreen(ScreensComponent screen) {
        if (!this.screens.contains(screen)) return;
        this.screens.remove(screen);
    }
}
