package com.necro.raid.dens.common.raids.battle;

import com.necro.raid.dens.common.showdown.events.ShowdownEvent;

import java.util.Optional;

public class TrainerSide extends RaidSide {
    public TrainerSide() {
        super(1);
    }

    @Override
    public Optional<ShowdownEvent> addSideCondition(String sideCondition) {
        return super.addSideCondition(sideCondition);
    }

    @Override
    public Optional<ShowdownEvent> removeSideCondition(String sideCondition) {
        return super.removeSideCondition(sideCondition);
    }
}
