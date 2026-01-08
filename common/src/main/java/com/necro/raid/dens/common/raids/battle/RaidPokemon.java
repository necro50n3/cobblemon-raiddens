package com.necro.raid.dens.common.raids.battle;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.pokemon.status.VolatileStatus;

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

    public void addStatus(Status status) {
        if (this.status != null) return;
        this.status = status;
    }

    public void removeStatus() {
        if (this.status == null) return;
        this.status = null;
    }

    public void addVolatile(VolatileStatus status) {
        if (this.volatileStatus.contains(status)) return;
        this.volatileStatus.add(status);
    }

    public void removeVolatile(VolatileStatus status) {
        if (!this.volatileStatus.contains(status)) return;
        this.volatileStatus.remove(status);
    }

    public void boost(Stat stat, int stages) {
        this.boosts.compute(stat, (oStat, oStages) -> {
            if (oStages == null) oStages = 0;
            return oStages + stages;
        });
    }

    public void clearPositiveBoosts(Stat stat) {
        int stages = this.boosts.getOrDefault(stat, 0);
        if (stages <= 0) return;
        this.boosts.remove(stat);
    }

    public void clearNegativeBoosts(Stat stat) {
        int stages = this.boosts.getOrDefault(stat, 0);
        if (stages >= 0) return;
        this.boosts.remove(stat);
    }

    public void clearBoosts(Stat stat) {
        int stages = this.boosts.getOrDefault(stat, 0);
        if (stages == 0) return;
        this.boosts.remove(stat);
    }
}
