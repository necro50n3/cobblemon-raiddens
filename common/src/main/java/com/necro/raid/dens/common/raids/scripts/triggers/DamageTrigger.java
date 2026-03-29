package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;
import kotlin.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DamageTrigger extends RaidTrigger<Pair<Float, UUID>> {
    private final float threshold;
    private final Set<UUID> claimed;

    public DamageTrigger(float threshold, List<AbstractEvent> events) {
        super(RaidTriggerType.DAMAGE, events);
        this.threshold = threshold;
        this.claimed = new HashSet<>();
    }

    @Override
    protected boolean check(Pair<Float, UUID> predicate) {
        boolean result = this.threshold <= predicate.getFirst() && !this.claimed.contains(predicate.getSecond());
        if (result) this.claimed.add(predicate.getSecond());
        return result;
    }

    @Override
    protected boolean shouldExpire() {
        return false;
    }
}
