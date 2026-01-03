package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidInstance;

public interface IRaidBattle {
    boolean isRaidBattle();
    RaidInstance getRaidBattle();
    void setRaidBattle(RaidInstance raidBattle);
}
