package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;

public interface RaidEvent extends AbstractEvent {
    void run(RaidInstance raid);
}
