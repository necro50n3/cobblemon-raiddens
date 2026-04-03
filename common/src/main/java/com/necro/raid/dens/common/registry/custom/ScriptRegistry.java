package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.raids.scripts.RaidScriptDecoder;
import com.necro.raid.dens.common.raids.scripts.ScriptDeserializer;
import com.necro.raid.dens.common.showdown.events.AbstractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        if (!(scripts instanceof List<?>)) scripts = List.of(scripts);
        return ((List<?>) scripts).stream().map(this::decode).filter(Objects::nonNull).toList();
    }
}
