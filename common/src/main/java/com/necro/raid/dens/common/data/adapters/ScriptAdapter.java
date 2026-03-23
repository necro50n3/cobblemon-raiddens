package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.necro.raid.dens.common.data.raid.Script;

import java.lang.reflect.Type;
import java.util.*;

public class ScriptAdapter implements JsonSerializer<Script>, JsonDeserializer<Script> {
    @Override
    public JsonElement serialize(Script src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (Object obj : src) {
            array.add(context.serialize(obj));
        }
        return array;
    }

    @Override
    public Script deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        Script result = new Script();

        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            result.add(json.getAsString());
            return result;
        }

        if (json.isJsonObject()) {
            result.add(toMap((JsonObject) json));
            return result;
        }

        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                if (element instanceof JsonPrimitive primitive && primitive.isString()) result.add(primitive.getAsString());
                else if (element instanceof JsonArray array) result.add(toList(array));
                else if (element instanceof JsonObject object) result.add(toMap(object));
            }
            return result;
        }

        throw new JsonParseException("Invalid script format: " + json);
    }

    private static List<?> toList(JsonArray element) {
        List<Object> list = new ArrayList<>();
        for (JsonElement json : element) {
            if (json instanceof JsonPrimitive primitive && primitive.isString()) list.add(primitive.getAsString());
            else if (json instanceof JsonPrimitive primitive && primitive.isNumber()) list.add(primitive.getAsNumber());
            else if (json instanceof JsonPrimitive primitive && primitive.isBoolean()) list.add(primitive.getAsBoolean());
            else if (json instanceof JsonArray array) list.add(toList(array));
            else if (json instanceof JsonObject object) list.add(toMap(object));
        }
        return list;
    }

    private static Map<?, ?> toMap(JsonObject element) {
        Map<String, Object> map = new HashMap<>();
        Set<String> keys = element.keySet();
        for (String key : keys) {
            JsonElement json = element.get(key);
            if (json instanceof JsonPrimitive primitive && primitive.isString()) map.put(key, primitive.getAsString());
            else if (json instanceof JsonPrimitive primitive && primitive.isNumber()) map.put(key, primitive.getAsNumber());
            else if (json instanceof JsonPrimitive primitive && primitive.isBoolean()) map.put(key, primitive.getAsBoolean());
            else if (json instanceof JsonArray array) map.put(key, toList(array));
            else if (json instanceof JsonObject object) map.put(key, toMap(object));
        }
        return map;
    }
}
