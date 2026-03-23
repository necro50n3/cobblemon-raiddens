package com.necro.raid.dens.common.data.raid;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonArray;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.DeserializationException;

import java.util.*;

public class Script extends ArrayList<Object> {
    public static Script deserialize(JsonElement json) throws DeserializationException {
        Script result = new Script();

        if (json instanceof JsonPrimitive primitive && primitive.getValue() instanceof String string) {
            result.add(string);
            return result;
        }

        if (json instanceof JsonObject jsonObject) {
            result.add(toMap(jsonObject));
            return result;
        }

        if (json instanceof JsonArray jsonArray) {
            for (JsonElement element : jsonArray) {
                if (element instanceof JsonPrimitive primitive && primitive.getValue() instanceof String string) result.add(string);
                else if (element instanceof JsonArray array) result.add(toList(array));
                else if (element instanceof JsonObject object) result.add(toMap(object));
            }
            return result;
        }

        throw new DeserializationException("Invalid script format: " + json);
    }

    private static List<?> toList(JsonArray element) {
        List<Object> list = new ArrayList<>();
        for (JsonElement json : element) {
            if (json instanceof JsonPrimitive primitive) list.add(primitive.getValue());
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
            if (json instanceof JsonPrimitive primitive) map.put(key, primitive.getValue());
            else if (json instanceof JsonArray array) map.put(key, toList(array));
            else if (json instanceof JsonObject object) map.put(key, toMap(object));
        }
        return map;
    }

    public static float toFloat(Object object) {
        return ((Number) object).floatValue();
    }

    public static double toDouble(Object object) {
        return ((Number) object).doubleValue();
    }

    public static int toInt(Object object) {
        return ((Number) object).intValue();
    }

    public static long toLong(Object object) {
        return ((Number) object).longValue();
    }
}
