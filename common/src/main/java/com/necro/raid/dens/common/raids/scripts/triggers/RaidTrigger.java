package com.necro.raid.dens.common.raids.scripts.triggers;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public abstract class RaidTrigger<T> {
    private final RaidTriggerType type;
    private final List<AbstractEvent> events;

    public RaidTrigger(RaidTriggerType type, List<AbstractEvent> events) {
        this.type = type;
        this.events = events;
    }

    public RaidTriggerType type() {
        return this.type;
    }

    protected abstract boolean check(T predicate);

    public boolean trigger(RaidInstance raid, PokemonBattle battle, T predicate) {
        if (!this.check(predicate)) return false;
        this.events.forEach(event -> raid.sendEvent(event, battle));
        return this.shouldExpire();
    }

    public boolean trigger(RaidInstance raid, T predicate) {
        return this.trigger(raid, null, predicate);
    }

    protected boolean shouldExpire() {
        return true;
    }
}
