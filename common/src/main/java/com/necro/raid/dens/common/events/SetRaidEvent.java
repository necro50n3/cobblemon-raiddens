package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;

public class SetRaidEvent {
    private RaidBoss raid;

    public SetRaidEvent(RaidBoss raid) {
        this.raid = raid;
    }

    public RaidBoss getRaid() {
        return this.raid;
    }

    public void setRaid(RaidBoss raid) {
        this.raid = raid;
    }
}
