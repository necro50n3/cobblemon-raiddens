package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class TimerTrigger extends RaidTrigger<Void> {
    private final int after;
    private final boolean repeat;

    public TimerTrigger(int after, boolean repeat, List<AbstractEvent> events) {
        super(RaidTriggerType.TIMER, events);
        this.after = after;
        this.repeat = repeat;
    }

    public int after() {
        return this.after;
    }

    public boolean repeat() {
        return this.repeat;
    }

    @Override
    protected boolean check(Void predicate) {
        return true;
    }

    @Override
    protected boolean shouldExpire() {
        return false;
    }
}
