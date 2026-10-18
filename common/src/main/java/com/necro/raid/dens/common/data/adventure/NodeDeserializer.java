package com.necro.raid.dens.common.data.adventure;

import com.google.gson.JsonSyntaxException;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;

import java.util.Map;

@FunctionalInterface
public interface NodeDeserializer {
    AdventureNode decode(Map<String,Object> script) throws ClassCastException, JsonSyntaxException;
}
