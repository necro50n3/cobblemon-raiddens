package com.necro.raid.dens.common.raids.battle;

import com.necro.raid.dens.common.showdown.events.ClearSideConditionShowdownEvent;
import com.necro.raid.dens.common.showdown.events.SetSideConditionShowdownEvent;
import com.necro.raid.dens.common.showdown.events.ShowdownEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class RaidSide {
    public Set<String> sideConditions;
    public final int side;

    public RaidSide(int side) {
        this.sideConditions = new HashSet<>();
        this.side = side;
    }

    public Optional<ShowdownEvent> addSideCondition(String sideCondition) {
        if (this.sideConditions.contains(sideCondition)) return Optional.empty();
        this.sideConditions.add(sideCondition);
        return Optional.of(new SetSideConditionShowdownEvent(sideCondition, this.side));
    }

    public Optional<ShowdownEvent> removeSideCondition(String sideCondition) {
        if (!this.sideConditions.contains(sideCondition)) return Optional.empty();
        this.sideConditions.remove(sideCondition);
        return Optional.of(new ClearSideConditionShowdownEvent(sideCondition, this.side));
    }
}
