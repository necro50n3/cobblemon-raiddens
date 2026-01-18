package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.data.raid.UniqueKey;

import java.lang.reflect.Type;

public class UniqueKeyAdapter implements JsonSerializer<UniqueKey>, JsonDeserializer<UniqueKey> {
    @Override
    public JsonElement serialize(UniqueKey src, Type typeOfSrc, JsonSerializationContext context) {
        return UniqueKey.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public UniqueKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return UniqueKey.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }
}