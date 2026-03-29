package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class JoinTrigger extends RaidTrigger<Integer> {
    private final int joins;
    private final boolean repeat;

    public JoinTrigger(int joins, boolean repeat, List<AbstractEvent> events) {
        super(RaidTriggerType.JOIN, events);
        this.joins = joins;
        this.repeat = repeat;
    }

    @Override
    protected boolean check(Integer predicate) {
        return this.joins <= predicate;
    }

    @Override
    protected boolean shouldExpire() {
        return !this.repeat;
    }
}
