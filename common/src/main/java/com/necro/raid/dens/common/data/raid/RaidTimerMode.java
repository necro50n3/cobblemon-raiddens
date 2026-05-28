package com.necro.raid.dens.common.data.raid;

public enum RaidTimerMode {
    GAME_TIME,
    SYSTEM_TIME,
    GLOBAL_GAME_TIME,
    GLOBAL_SYSTEM_TIME;

    public boolean isGlobal() {
        return this == GLOBAL_GAME_TIME || this == GLOBAL_SYSTEM_TIME;
    }
}
