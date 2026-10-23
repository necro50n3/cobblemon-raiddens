package com.necro.raid.dens.common.data.adventure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.necro.raid.dens.common.data.RaidSession;
import com.necro.raid.dens.common.data.adapters.NodeAdapter;
import com.necro.raid.dens.common.data.adapters.NodeListAdapter;
import com.necro.raid.dens.common.data.adapters.RaidBossAdapter;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import com.necro.raid.dens.common.raids.adventure.BossNode;

import java.util.ArrayList;
import java.util.List;

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
    public RaidBoss instance() {
        return this.boss();
    }

    @Override
    public void applyDefaults() {
        if (this.nodes == null) throw new JsonSyntaxException("Missing required field: \"nodes\"");
        if (this.boss == null) throw new JsonSyntaxException("Missing required field: \"boss\"");
        this.nodes = new ArrayList<>(this.nodes);
        this.nodes.add(new BossNode());
        this.boss.applyDefaults();
        this.setIsAdventure(true);
    }

    @Override
    public boolean isAdventure() {
        return this.isAdventure;
    }

    @Override
    public void setIsAdventure(boolean isAdventure) {
        this.isAdventure = isAdventure;
        this.boss.setIsAdventure(isAdventure);
    }
}
