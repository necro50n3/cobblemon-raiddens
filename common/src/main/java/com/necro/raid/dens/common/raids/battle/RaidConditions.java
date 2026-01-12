package com.necro.raid.dens.common.raids.battle;

import com.necro.raid.dens.common.CobblemonRaidDens;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RaidConditions {
    public static final Set<String> PRIMAL_WEATHER = new HashSet<>();
    public static final Set<String> SCREENS = new HashSet<>();
    public static final Set<String> TAILWIND = new HashSet<>();
    public static final Set<String> HAZARDS = new HashSet<>();
    public static final Set<String> TERRAIN = new HashSet<>();

    public static void init() {
        PRIMAL_WEATHER.addAll(List.of(CobblemonRaidDens.CONDITIONS_CONFIG.primal_weather));
        SCREENS.addAll(List.of(CobblemonRaidDens.CONDITIONS_CONFIG.screens));
        TAILWIND.addAll(List.of(CobblemonRaidDens.CONDITIONS_CONFIG.tailwind));
        HAZARDS.addAll(List.of(CobblemonRaidDens.CONDITIONS_CONFIG.hazards));
        TERRAIN.addAll(List.of(CobblemonRaidDens.CONDITIONS_CONFIG.terrain));
    }
}
