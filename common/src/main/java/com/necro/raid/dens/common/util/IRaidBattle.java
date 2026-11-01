package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidSession;

public interface IRaidBattle {
    boolean crd_isRaid();
    RaidSession crd_getRaidSession();
    void crd_setRaidSession(RaidSession raid);
}
