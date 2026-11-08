package com.necro.raid.dens.common.compat.genesisforms;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import me.unariginal.genesisforms.config.ConfigManager;

public class RaidDensGenesisCompat {
    public static void setupTera(PokemonEntity pokemonEntity, Pokemon pokemon) {
        if (ConfigManager.EVENTS.terastallization == null) return;
        pokemon.getPersistentData().putString("tera_type", pokemon.getTeraType().showdownId());
        ConfigManager.EVENTS.terastallization.runEvent(pokemon.getTeraType().showdownId(), pokemon, pokemonEntity);
    }

    public static void setupDmax(PokemonEntity pokemonEntity, Pokemon pokemon) {
        if (ConfigManager.EVENTS.dynamax == null) return;
        ConfigManager.EVENTS.dynamax.runEvent("global", pokemon, pokemonEntity);
    }
}
