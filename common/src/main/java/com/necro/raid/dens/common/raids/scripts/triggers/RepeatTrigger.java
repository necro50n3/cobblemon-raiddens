package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class RepeatTrigger extends RaidTrigger<Void> {
    private int repeats;

    public RepeatTrigger(RaidTriggerType type, int repeats, List<AbstractEvent> events) {
        super(type, events);
        this.repeats = repeats;
    }

    @Override
    protected boolean check(Void predicate) {
        return true;
    }

    @Override
    protected boolean shouldExpire() {
        return this.repeats != -1 && --this.repeats <= 0;
    }
}
