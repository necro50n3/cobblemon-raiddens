package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.data.raid.Script;

import java.lang.reflect.Type;

public class ScriptAdapter implements JsonSerializer<Script>, JsonDeserializer<Script> {
    @Override
    public JsonElement serialize(Script src, Type typeOfSrc, JsonSerializationContext context) {
        return Script.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public Script deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return Script.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }
}
