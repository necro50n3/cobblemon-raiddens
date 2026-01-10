package com.necro.raid.dens.common.raids.battle;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;
import com.necro.raid.dens.common.showdown.events.*;
import net.minecraft.util.Mth;

import java.util.*;

public class RaidPokemon {
    private Status status;
    private final Set<VolatileStatus> volatileStatus;
    private final Map<Stat, Integer> boosts;

    public RaidPokemon() {
        this.status = null;
        this.volatileStatus = new HashSet<>();
        this.boosts = new HashMap<>();
    }

    public Optional<ShowdownEvent> addStatus(Status status) {
        if (this.status != null) return Optional.empty();
        this.status = status;
        return Optional.of(new SetStatusShowdownEvent(status, 2));
    }

    public Optional<ShowdownEvent> removeStatus() {
        if (this.status == null) return Optional.empty();
        this.status = null;
        return Optional.of(new CureStatusShowdownEvent(null, 2));
    }

    public Optional<ShowdownEvent> addVolatile(VolatileStatus status) {
        if (this.volatileStatus.contains(status)) return Optional.empty();
        this.volatileStatus.add(status);
        return Optional.of(new SetStatusShowdownEvent(status, 2));
    }

    public Optional<ShowdownEvent> removeVolatile(VolatileStatus status) {
        if (!this.volatileStatus.contains(status)) return Optional.empty();
        this.volatileStatus.remove(status);
        return Optional.of(new CureStatusShowdownEvent(status, 2));
    }

    public Optional<ShowdownEvent> boost(Stat stat, int boost) {
        int originalStages = this.boosts.getOrDefault(stat, 0);
        int stages = Mth.clamp(originalStages + boost, -6, 6);
        if (originalStages == stages) return Optional.empty();
        this.boosts.put(stat, stages);
        boost = stages - originalStages;
        return Optional.of(new StatBoostShowdownEvent(stat, boost, 2, true));
    }

    public Optional<ShowdownEvent> setBoost(Stat stat, int boost) {
        int stages = this.boosts.getOrDefault(stat, 0);
        if (stages == boost) return Optional.empty();
        this.boosts.put(stat, boost);
        return Optional.of(new SetBoostShowdownEvent(stat, boost, 2));
    }

    public Optional<ShowdownEvent> clearBoosts() {
        if (this.boosts.isEmpty()) return Optional.empty();
        this.boosts.clear();
        return Optional.of(new ClearBoostShowdownEvent(2));
    }

    public Optional<ShowdownEvent> clearNegativeBoosts() {
        boolean wasCleared = false;
        Iterator<Map.Entry<Stat, Integer>> it = boosts.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue() < 0) {
                it.remove();
                wasCleared = true;
            }
        }
        if (!wasCleared) return Optional.empty();
        return Optional.of(new ClearNegativeBoostShowdownEvent(2));
    }

    public Optional<ShowdownEvent> invertBoosts() {
        if (this.boosts.isEmpty()) return Optional.empty();
        this.boosts.replaceAll((stat, stages) -> -stages);
        return Optional.of(new InvertBoostShowdownEvent(2));
    }
}
