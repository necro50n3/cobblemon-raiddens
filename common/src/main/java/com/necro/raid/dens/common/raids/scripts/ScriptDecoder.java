package com.necro.raid.dens.common.raids.scripts;

import com.necro.raid.dens.common.showdown.events.AbstractEvent;

@FunctionalInterface
public interface ScriptDecoder {
    AbstractEvent decode(String[] args);
}
