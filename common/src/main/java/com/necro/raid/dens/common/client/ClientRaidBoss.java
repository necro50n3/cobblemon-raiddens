package com.necro.raid.dens.common.client;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.pokemon.Gender;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ClientRaidBoss {
    private static Optional<String> getGender(PokemonProperties properties) {
        Gender gender = properties.getGender();
        if (gender == null) return Optional.empty();
        else return Optional.of(gender.getSerializedName());
    }

    private static Optional<RaidFeature> getFeature(RaidBoss raidBoss) {
        return Optional.ofNullable(raidBoss.getFeature());
    }

    private static Optional<List<SpeciesFeature>> getRaidForm(RaidBoss raidBoss) {
        return Optional.ofNullable(raidBoss.getRaidForm());
    }

    private static Optional<List<SpeciesFeature>> getBaseForm(RaidBoss raidBoss) {
        return Optional.ofNullable(raidBoss.getBaseForm());
    }

    private static Optional<Float> getShinyRate(RaidBoss raidBoss) {
        return Optional.of(raidBoss.getShinyRate());
    }

    private static Optional<Integer> getMaxCatches(RaidBoss raidBoss) {
        return Optional.of(raidBoss.getMaxCatches());
    }

    private static Codec<PokemonProperties> propertiesCodec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("species").forGetter(PokemonProperties::getSpecies),
                Codec.STRING.optionalFieldOf("gender").forGetter(ClientRaidBoss::getGender)
            ).apply(inst, (species, gender) -> {
                PokemonProperties properties = PokemonProperties.Companion.parse("");
                properties.setSpecies(species);
                try { gender.ifPresent(s -> properties.setGender(Gender.valueOf(s))); }
                catch (IllegalArgumentException ignored) {}
                return properties;
            })
        );
    }

    public static Codec<RaidBoss> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
                propertiesCodec().fieldOf("pokemon").forGetter(RaidBoss::getProperties),
                RaidTier.codec().fieldOf("raid_tier").forGetter(RaidBoss::getTier),
                RaidType.codec().fieldOf("raid_type").forGetter(RaidBoss::getType),
                RaidFeature.codec().optionalFieldOf("raid_feature").forGetter(ClientRaidBoss::getFeature),
                RaidBoss.raidFormCodec().listOf().optionalFieldOf("raid_form").forGetter(ClientRaidBoss::getRaidForm),
                RaidBoss.raidFormCodec().listOf().optionalFieldOf("base_form").forGetter(ClientRaidBoss::getBaseForm),
                Codec.FLOAT.optionalFieldOf("shiny_rate").forGetter(ClientRaidBoss::getShinyRate),
                Codec.INT.optionalFieldOf("max_catches").forGetter(ClientRaidBoss::getMaxCatches)
            ).apply(inst, (properties, tier, type, oFeature, oRaidForm, oBaseForm, oShinyRate, oMaxCatches) -> {
                if (oShinyRate.isEmpty()) properties.setShiny(false);
                else if (oShinyRate.get() == 1.0f) properties.setShiny(true);

                List<SpeciesFeature> raidForm = new ArrayList<>();
                oRaidForm.ifPresent(raidForm::addAll);
                oBaseForm.ifPresent(raidForm::addAll);

                oFeature.ifPresent(feature -> {
                    if (feature == RaidFeature.DYNAMAX && raidForm.stream().noneMatch(form -> form.getName().equals("dynamax_form"))) {
                        raidForm.add(new StringSpeciesFeature("dynamax_form", "none"));
                    }
                    else if (feature == RaidFeature.MEGA && raidForm.stream().noneMatch(form -> form.getName().equals("mega_evolution"))) {
                        raidForm.add(new StringSpeciesFeature("mega_evolution", "mega"));
                    }
                });

                return new RaidBoss(
                    properties, tier, type, oFeature.orElse(RaidFeature.DEFAULT), raidForm, new ArrayList<>(), null,
                    0.0, oMaxCatches.orElse(-1), 0, oShinyRate.orElse(0.0f),
                    new HashMap<>(), new ArrayList<>(), "", 0
                );
            })
        );
    }
}
