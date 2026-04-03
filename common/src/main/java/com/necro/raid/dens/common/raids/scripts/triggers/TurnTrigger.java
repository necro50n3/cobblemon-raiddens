package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class TurnTrigger extends RaidTrigger<Integer> {
    private final int turn;
    private final boolean repeat;

    public TurnTrigger(int turn, boolean repeat, List<AbstractEvent> events) {
        super(RaidTriggerType.TURN, events);
        this.turn = Math.max(1, turn);
        this.repeat = repeat;
    }

    @Override
    protected boolean check(Integer predicate) {
        return predicate % this.turn == 0;
    }

    @Override
    protected boolean shouldExpire() {
        return !this.repeat;
    }
}
