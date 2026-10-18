package com.necro.raid.dens.common.data;

import com.cobblemon.mod.common.pokemon.Species;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.data.raid.UniqueKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Set;

public interface RaidSession {
    void createDisplayAspects();

    void setDisplaySpecies(ResourceLocation species);

    void setDisplayAspects(Set<String> aspects);

    void applyDefaults();

    ResourceLocation getId();

    RaidTier getTier();
    
    RaidType getType();

    String getFeature();

    Double getWeight();

    List<String> getDens();

    UniqueKey getKey();

    Integer getMaxPlayers();

    Integer getMaxClears();

    Integer getLives();

    Boolean getPlayersShareLives();

    Species getDisplaySpecies();

    ResourceLocation getDisplaySpeciesIdentifier();

    Set<String> getDisplayAspects();

    void setId(ResourceLocation id);

    ResourceLocation getRandomDen(RandomSource random);

    boolean isAdventure();
}
