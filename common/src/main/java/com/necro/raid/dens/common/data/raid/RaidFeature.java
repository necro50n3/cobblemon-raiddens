package com.necro.raid.dens.common.data.raid;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.compat.megashowdown.RaidDensMSDCompat;
import com.necro.raid.dens.common.compat.shadowedhearts.RaidDensShadowedHeartsCompat;

import java.util.List;
import java.util.Set;

public interface RaidFeature {
    String getId();

    void applyAspects(Set<String> aspects, List<CustomPokemonProperty> customProperties);

    void applyToBoss(Pokemon pokemon, PokemonEntity pokemonEntity);

    void applyToReward(Pokemon pokemon);

    static String getTranslatable(String id) {
        return "feature.cobblemonraiddens." + id.toLowerCase();
    }

    enum Base implements RaidFeature {
        DEFAULT {
            @Override
            public String getId() {
                return "default";
            }

            @Override
            public void applyAspects(Set<String> aspects, List<CustomPokemonProperty> customProperties) {}

            @Override
            public void applyToBoss(Pokemon pokemon, PokemonEntity pokemonEntity) {}

            @Override
            public void applyToReward(Pokemon pokemon) {}
        },

        MEGA{
            @Override
            public String getId() {
                return "mega";
            }

            @Override
            public void applyAspects(Set<String> aspects, List<CustomPokemonProperty> customProperties) {
                if (customProperties.stream().filter(SpeciesFeature.class::isInstance).noneMatch(prop -> ((SpeciesFeature) prop).getName().equals("mega_evolution"))) {
                    customProperties.add(new StringSpeciesFeature("mega_evolution", "mega"));
                }
            }

            @Override
            public void applyToBoss(Pokemon pokemon, PokemonEntity pokemonEntity) {}

            @Override
            public void applyToReward(Pokemon pokemon) {}
        },

        TERA{
            @Override
            public String getId() {
                return "tera";
            }

            @Override
            public void applyAspects(Set<String> aspects, List<CustomPokemonProperty> customProperties) {}

            @Override
            public void applyToBoss(Pokemon pokemon, PokemonEntity pokemonEntity) {
                RaidDensMSDCompat.setupTera(pokemon);
            }

            @Override
            public void applyToReward(Pokemon pokemon) {}
        },

        DYNAMAX{
            @Override
            public String getId() {
                return "dynamax";
            }

            @Override
            public void applyAspects(Set<String> aspects, List<CustomPokemonProperty> customProperties) {
                if (customProperties.stream().filter(SpeciesFeature.class::isInstance).noneMatch(prop -> ((SpeciesFeature) prop).getName().equals("dynamax_form"))) {
                    customProperties.add(new StringSpeciesFeature("dynamax_form", "none"));
                }
            }

            @Override
            public void applyToBoss(Pokemon pokemon, PokemonEntity pokemonEntity) {
                RaidDensMSDCompat.setupDmax(pokemonEntity, pokemon);
            }

            @Override
            public void applyToReward(Pokemon pokemon) {
                pokemon.setDmaxLevel(Cobblemon.config.getMaxDynamaxLevel());
                if (new StringSpeciesFeature("dynamax_form", "gmax").matches(pokemon)) pokemon.setGmaxFactor(true);
            }
        },

        SHADOW{
            @Override
            public String getId() {
                return "shadow";
            }

            @Override
            public void applyAspects(Set<String> aspects, List<CustomPokemonProperty> customProperties) {}

            @Override
            public void applyToBoss(Pokemon pokemon, PokemonEntity pokemonEntity) {
                RaidDensShadowedHeartsCompat.setShadowBoss(pokemon, pokemonEntity);
            }

            @Override
            public void applyToReward(Pokemon pokemon) {
                RaidDensShadowedHeartsCompat.setShadowReward(pokemon);
            }
        }
    }
}
