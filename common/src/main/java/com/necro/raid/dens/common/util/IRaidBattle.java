package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidInstance;

public interface IRaidBattle {
    boolean crd_isRaidBattle();
    RaidInstance crd_getRaidBattle();
    void crd_setRaidBattle(RaidInstance raidBattle);
}
