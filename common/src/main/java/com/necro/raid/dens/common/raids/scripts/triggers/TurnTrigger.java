package com.necro.raid.dens.common.raids.scripts.triggers;

import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.List;

public class TurnTrigger extends RaidTrigger<Integer> {
    private final int turn;

    public TurnTrigger(int turn, List<AbstractEvent> events) {
        super(RaidTriggerType.TURN, events);
        this.turn = turn;
    }

    @Override
    protected boolean check(Integer predicate) {
        return this.turn == predicate;
    }
}
