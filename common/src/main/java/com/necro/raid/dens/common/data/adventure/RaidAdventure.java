package com.necro.raid.dens.common.data.adventure;

import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.necro.raid.dens.common.data.RaidSession;
import com.necro.raid.dens.common.data.adapters.NodeAdapter;
import com.necro.raid.dens.common.data.adapters.NodeListAdapter;
import com.necro.raid.dens.common.data.adapters.RaidBossAdapter;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.data.raid.UniqueKey;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import com.necro.raid.dens.common.raids.adventure.BossNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RaidAdventure implements RaidSession {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(AdventureNode.class, NodeAdapter.INSTANCE)
        .registerTypeAdapter(new TypeToken<List<AdventureNode>>(){}.getType(), NodeListAdapter.INSTANCE)
        .registerTypeAdapter(RaidBoss.class, RaidBossAdapter.INSTANCE)
        .create();

    private List<AdventureNode> nodes;
    private RaidBoss boss;
    private transient boolean isAdventure;

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
        this.nodes = new ArrayList<>(this.nodes);
        this.nodes.add(new BossNode());
        this.boss.applyDefaults();
        this.isAdventure = true;
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

    @Override
    public boolean isAdventure() {
        return this.isAdventure;
    }

    @Override
    public void setIsAdventure(boolean isAdventure) {
        this.isAdventure = isAdventure;
    }
}
