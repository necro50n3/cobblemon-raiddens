package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;

public class SetRaidBossEvent {
    private RaidBoss raidBoss;

    public SetRaidBossEvent(RaidBoss raidBoss) {
        this.raidBoss = raidBoss;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public void setRaidBoss(RaidBoss raidBoss) {
        this.raidBoss = raidBoss;
    }
}
