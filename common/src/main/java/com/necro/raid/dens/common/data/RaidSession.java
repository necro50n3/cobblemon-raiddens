package com.necro.raid.dens.common.data;

import com.necro.raid.dens.common.data.raid.RaidBoss;

public interface RaidSession {
    RaidBoss instance();

    void applyDefaults();

    boolean isAdventure();

    void setIsAdventure(boolean isAdventure);
}
