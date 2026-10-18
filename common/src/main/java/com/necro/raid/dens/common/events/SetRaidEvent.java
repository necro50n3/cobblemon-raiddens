package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.RaidSession;

public class SetRaidEvent {
    private RaidSession raid;

    public SetRaidEvent(RaidSession raid) {
        this.raid = raid;
    }

    public RaidSession getRaid() {
        return this.raid;
    }

    public void setRaid(RaidSession raid) {
        this.raid = raid;
    }
}
