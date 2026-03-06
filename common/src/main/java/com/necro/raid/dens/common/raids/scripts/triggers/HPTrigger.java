package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class HPTrigger extends RaidTrigger<Float> {
    private final float threshold;

    public HPTrigger(float threshold, List<AbstractEvent> events) {
        super(RaidTriggerType.HP, events);
        this.threshold = threshold;
    }

    @Override
    protected boolean check(Float predicate) {
        return this.threshold >= predicate;
    }
}
