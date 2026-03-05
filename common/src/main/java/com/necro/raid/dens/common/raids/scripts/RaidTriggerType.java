package com.necro.raid.dens.common.raids.scripts;

import com.necro.raid.dens.common.raids.scripts.triggers.FaintTrigger;
import com.necro.raid.dens.common.raids.scripts.triggers.HPTrigger;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import com.necro.raid.dens.common.raids.scripts.triggers.TurnTrigger;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;
import kotlin.jvm.functions.Function2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RaidTriggerType {
    TURN,
    HP,
    FAINT;

    private static final Map<String, Function2<String, List<AbstractEvent>, RaidTrigger<?>>> PARSER = new HashMap<>();

    public static RaidTrigger<?> decode(String key, List<AbstractEvent> events) {
        String[] split = key.split(":");
        Function2<String, List<AbstractEvent>, ? extends RaidTrigger<?>> parser = PARSER.get(split[0]);
        if (parser == null) return null;
        return parser.invoke(split[1], events);
    }

    static {
        PARSER.put("turn", (turn, events) -> new TurnTrigger(Integer.parseInt(turn), events));
        PARSER.put("hp", (hp, events) -> new HPTrigger(Double.parseDouble(hp), events));
        PARSER.put("faint", (repeats, events) -> new FaintTrigger(Integer.parseInt(repeats), events));
    }
}
