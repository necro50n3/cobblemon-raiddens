package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.data.adventure.NodeDeserializer;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

public class NodeRegistry extends JsonRegistry<NodeDeserializer> {
    public NodeRegistry() {
        super(null);
    }

    public @Nullable AdventureNode decode(Object node) {
        if (!(node instanceof Map<?,?> map)) return null;
        Map<String, Object> safeMap = map.entrySet().stream()
            .filter(e -> e.getKey() instanceof String)
            .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));
        String type = (String) safeMap.get("type");
        if (type == null) return null;
        NodeDeserializer deserializer = this.get(type);
        return deserializer == null ? null : deserializer.decode(safeMap);
    }
}
