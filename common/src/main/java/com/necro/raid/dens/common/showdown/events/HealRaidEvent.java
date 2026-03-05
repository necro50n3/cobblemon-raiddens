package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;

public record HealRaidEvent(float ratio) implements RaidEvent {
    @Override
    public void run(RaidInstance raid) {
        raid.healBoss(this.ratio);
    }
}
