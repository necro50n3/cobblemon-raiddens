package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;

import java.lang.reflect.Type;
import java.util.*;

public class NodeListAdapter implements JsonSerializer<List<AdventureNode>>, JsonDeserializer<List<AdventureNode>> {
    public static final NodeListAdapter INSTANCE = new NodeListAdapter();

    @Override
    public List<AdventureNode> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonArray()) throw new JsonParseException("Expected a JSON array");
        List<AdventureNode> result = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray()) {
            result.add(NodeAdapter.INSTANCE.deserialize(element, typeOfT, context));
        }
        return result;
    }

    @Override
    public JsonElement serialize(List<AdventureNode> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        src.forEach(node -> array.add(node.serialize(node, typeOfSrc, context)));
        return array;
    }
}
