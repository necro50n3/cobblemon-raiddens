package com.necro.raid.dens.common.raids.scripts;

import com.google.gson.JsonSyntaxException;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;

import java.util.Map;

@FunctionalInterface
public interface ScriptDeserializer {
    AbstractEvent decode(Map<String,Object> script) throws ClassCastException, JsonSyntaxException;
}
