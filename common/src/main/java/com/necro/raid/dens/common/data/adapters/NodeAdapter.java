package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.necro.raid.dens.common.data.adventure.NodeDeserializer;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;

import java.lang.reflect.Type;
import java.util.*;

public class NodeAdapter implements JsonSerializer<AdventureNode>, JsonDeserializer<AdventureNode> {
    public static final NodeAdapter INSTANCE = new NodeAdapter();

    @Override
    public AdventureNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) throw new JsonParseException("Node is not in the correct format");
        JsonObject object = json.getAsJsonObject();
        if (!object.has("type")) throw new JsonParseException("Missing required parameter \"type\"");
        NodeDeserializer deserializer = CustomRaidRegistries.NODE_REGISTRY.get(object.get("type").getAsString());
        if (deserializer == null) throw new JsonParseException("Unrecognised \"type\" for node");
        return deserializer.decode(toMap((JsonObject) json));
    }

    @Override
    public JsonElement serialize(AdventureNode src, Type typeOfSrc, JsonSerializationContext context) {
        return src.serialize(src, typeOfSrc, context);
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

    private static Map<String, Object> toMap(JsonObject element) {
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
