package com.necro.raid.dens.common.registry.generator;

import com.necro.raid.dens.common.CobblemonRaidDens;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneratorHelper {
    public static Set<String> WHITELIST = new HashSet<>();
    public static Set<String> BLACKLIST = new HashSet<>();
    public static Set<String> LABELS = new HashSet<>();

    public static void init() {
        WHITELIST.addAll(List.of(CobblemonRaidDens.GENERATOR_CONFIG.pokemon_whitelist));
        BLACKLIST.addAll(List.of(CobblemonRaidDens.GENERATOR_CONFIG.pokemon_blacklist));
        LABELS.addAll(List.of(CobblemonRaidDens.GENERATOR_CONFIG.label_blacklist));
    }
}
