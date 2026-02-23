package com.necro.raid.dens.common.raids.scripts;

import com.necro.raid.dens.common.showdown.events.ShowdownEvent;

@FunctionalInterface
public interface ScriptDecoder {
    ShowdownEvent decode(String[] args);
}
