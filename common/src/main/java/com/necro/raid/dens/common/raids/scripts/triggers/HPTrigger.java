package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class HPTrigger extends RaidTrigger<Double> {
    private final double threshold;

    public HPTrigger(double threshold, List<AbstractEvent> events) {
        super(RaidTriggerType.HP, events);
        this.threshold = threshold;
    }

    @Override
    protected boolean check(Double predicate) {
        return this.threshold >= predicate;
    }
}
