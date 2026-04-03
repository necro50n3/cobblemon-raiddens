package com.necro.raid.dens.common.registry.custom;

import com.google.gson.JsonSyntaxException;
import com.necro.raid.dens.common.raids.scripts.RaidScriptDecoder;
import com.necro.raid.dens.common.raids.scripts.ScriptDeserializer;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScriptRegistry extends StringRegistry<ScriptDeserializer> {
    public ScriptRegistry() {
        super(null);
    }

    public @Nullable AbstractEvent decode(Object script) {
        if (script instanceof String string) {
            return RaidScriptDecoder.decode(string);
        }
        else if (script instanceof Map<?,?> map) {
            Map<String, Object> safeMap = map.entrySet().stream()
                .filter(e -> e.getKey() instanceof String)
                .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));
            String type = (String) safeMap.get("type");
            if (type == null) return null;
            ScriptDeserializer deserializer = this.get(type);
            return deserializer == null ? null : deserializer.decode(safeMap);
        }
        return null;
    }

    public List<AbstractEvent> decodeList(Object scripts) {
        if (scripts == null) throw new JsonSyntaxException("Missing field \"scripts\"");
        if (!(scripts instanceof List<?>)) scripts = List.of(scripts);
        List<AbstractEvent> events = ((List<?>) scripts).stream().map(this::decode).filter(Objects::nonNull).toList();
        if (events.isEmpty()) throw new JsonSyntaxException("Failed to parse field \"scripts\"");
        return events;
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
}
