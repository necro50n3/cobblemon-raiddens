package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class TurnTrigger extends RaidTrigger<Integer> {
    private final int turn;
    private final boolean repeat;
    private int lastTurn;
    private int offset;

    public TurnTrigger(int turn, boolean repeat, List<AbstractEvent> events) {
        super(RaidTriggerType.TURN, events);
        this.turn = turn;
        this.repeat = repeat;
        this.lastTurn = -1;
        this.offset = 0;
    }

    @Override
    protected boolean check(Integer predicate) {
        if (predicate <= this.lastTurn) return false;
        boolean result = (predicate - this.offset) % this.turn == 0;
        if (result) this.lastTurn = predicate;
        return result;
    }

    @Override
    protected boolean shouldExpire() {
        return !this.repeat;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
