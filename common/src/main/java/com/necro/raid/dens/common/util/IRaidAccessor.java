package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidBoss;

import java.util.UUID;

public interface IRaidAccessor {
    UUID getRaidId();

    void setRaidId(UUID raidId);

    RaidBoss getRaidBoss();

    void setRaidBoss(RaidBoss raidBoss);

    boolean isRaidBoss();
}
