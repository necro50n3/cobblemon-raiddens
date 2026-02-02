package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.necro.raid.dens.common.data.raid.RaidBoss;

import java.lang.reflect.Type;

public class RaidBossAdapter implements JsonSerializer<RaidBoss>, JsonDeserializer<RaidBoss> {
    @Override
    public RaidBoss deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return RaidBoss.GSON.fromJson(json, RaidBoss.class);
    }

    @Override
    public JsonElement serialize(RaidBoss src, Type typeOfSrc, JsonSerializationContext context) {
        return RaidBoss.GSON.toJsonTree(src);
    }
}
