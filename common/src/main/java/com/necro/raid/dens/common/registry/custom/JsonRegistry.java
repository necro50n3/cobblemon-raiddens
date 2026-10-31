package com.necro.raid.dens.common.registry.custom;

import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class JsonRegistry<U> extends StringRegistry<U> {
    public JsonRegistry(String defaultKey) {
        super(defaultKey);
    }

    public <T, R> R transform(Map<String, Object> script, String key, Class<T> clazz, Function<T, R> transform) {
        return transform.apply(this.parse(script, key, clazz));
    }

    public <T, R> R transform(Map<String, Object> script, String key, Class<T> clazz, Function<T, R> transform, R defaultValue) {
        Object value = script.get(key);
        if (value == null) return defaultValue;
        return transform.apply(this.parseCommon(value, key, clazz));
    }

    public <T> T parse(Map<String, Object> script, String key, Class<T> clazz) {
        Object value = script.get(key);
        return this.parseCommon(value, key, clazz);
    }

    public <T> T parse(Map<String, Object> script, String key, Class<T> clazz, T defaultValue) {
        Object value = script.get(key);
        if (value == null) return defaultValue;
        return this.parseCommon(value, key, clazz);
    }

    private <T> T parseCommon(Object value, String key, Class<T> clazz) throws JsonSyntaxException {
        if (value == null) throw new JsonSyntaxException("Missing field \"" + key + "\"");
        else if (!clazz.isInstance(value)) throw new JsonSyntaxException("Failed to parse field \"" + key + "\"");
        return clazz.cast(value);
    }

    public <T> List<T> parseList(Map<String, Object> script, String key, Class<T> elementClass) {
        Object value = script.get(key);
        return this.parseListCommon(value, key, elementClass);
    }

    public <T> List<T> parseList(Map<String, Object> script, String key, Class<T> elementClass, List<T> defaultValue) {
        Object value = script.get(key);
        if (value == null) return defaultValue;
        return this.parseListCommon(value, key, elementClass);
    }

    private <T> List<T> parseListCommon(Object value, String key, Class<T> elementClass) throws JsonSyntaxException {
        if (value == null) throw new JsonSyntaxException("Missing field \"" + key + "\"");
        if (!(value instanceof List<?> raw)) throw new JsonSyntaxException("Field \"" + key + "\" is not a list");

        List<T> result = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) result.add(this.parseCommon(raw.get(i), key + "[" + i + "]", elementClass));
        return result;
    }
}
