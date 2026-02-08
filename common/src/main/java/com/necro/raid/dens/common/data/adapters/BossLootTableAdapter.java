package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.data.raid.BossLootTable;

import java.lang.reflect.Type;

public class BossLootTableAdapter implements JsonSerializer<BossLootTable>, JsonDeserializer<BossLootTable> {
    @Override
    public JsonElement serialize(BossLootTable src, Type typeOfSrc, JsonSerializationContext context) {
        return BossLootTable.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public BossLootTable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return BossLootTable.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }
}
