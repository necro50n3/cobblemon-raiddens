package com.necro.raid.dens.common.data.adventure;

import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonSyntaxException;
import com.necro.raid.dens.common.data.RaidSession;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.data.raid.UniqueKey;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Set;

public class RaidAdventure implements RaidSession {
    private final List<AdventureNode> nodes;
    private final RaidBoss boss;

    public RaidAdventure(List<AdventureNode> nodes, RaidBoss boss) {
        this.nodes = nodes;
        this.boss = boss;
    }

    public List<AdventureNode> nodes() {
        return this.nodes;
    }

    public RaidBoss boss() {
        return this.boss;
    }

    @Override
    public void createDisplayAspects() {
        this.boss.createDisplayAspects();
    }

    @Override
    public void setDisplaySpecies(ResourceLocation species) {
        this.boss.setDisplaySpecies(species);
    }

    @Override
    public void setDisplayAspects(Set<String> aspects) {
        this.boss.setDisplayAspects(aspects);
    }

    @Override
    public void applyDefaults() {
        if (this.nodes == null) throw new JsonSyntaxException("Missing required field: \"nodes\"");
        if (this.boss == null) throw new JsonSyntaxException("Missing required field: \"boss\"");
        this.boss.applyDefaults();
    }

    @Override
    public ResourceLocation getId() {
        return this.boss.getId();
    }

    @Override
    public RaidTier getTier() {
        return this.boss.getTier();
    }

    @Override
    public RaidType getType() {
        return this.boss.getType();
    }

    @Override
    public String getFeature() {
        return this.boss.getFeature();
    }

    @Override
    public Double getWeight() {
        return this.boss.getWeight();
    }

    @Override
    public List<String> getDens() {
        return this.boss.getDens();
    }

    @Override
    public UniqueKey getKey() {
        return this.boss.getKey();
    }

    @Override
    public Integer getMaxPlayers() {
        return this.boss.getMaxPlayers();
    }

    @Override
    public Integer getMaxClears() {
        return this.boss.getMaxClears();
    }

    @Override
    public Integer getLives() {
        return this.boss.getLives();
    }

    @Override
    public Boolean getPlayersShareLives() {
        return this.boss.getPlayersShareLives();
    }

    @Override
    public Species getDisplaySpecies() {
        return this.boss.getDisplaySpecies();
    }

    @Override
    public ResourceLocation getDisplaySpeciesIdentifier() {
        return this.boss.getDisplaySpeciesIdentifier();
    }

    @Override
    public Set<String> getDisplayAspects() {
        return this.boss.getDisplayAspects();
    }

    @Override
    public void setId(ResourceLocation id) {
        this.boss.setId(id);
    }

    @Override
    public ResourceLocation getRandomDen(RandomSource random) {
        return this.boss.getRandomDen(random);
    }

    public boolean isAdventure() {
        return true;
    }
}
