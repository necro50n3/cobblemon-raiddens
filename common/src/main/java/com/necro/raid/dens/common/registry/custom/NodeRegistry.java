package com.necro.raid.dens.common.registry.custom;

import com.google.gson.JsonSyntaxException;
import com.necro.raid.dens.common.data.adventure.NodeDeserializer;
import com.necro.raid.dens.common.raids.adventure.AdventureNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NodeRegistry extends JsonRegistry<NodeDeserializer> {
    public NodeRegistry() {
        super(null);
    }

    public @Nullable AdventureNode decode(Object script) {
        if (!(script instanceof Map<?,?> map)) return null;
        Map<String, Object> safeMap = map.entrySet().stream()
            .filter(e -> e.getKey() instanceof String)
            .collect(Collectors.toMap(e -> (String) e.getKey(), Map.Entry::getValue));
        String type = (String) safeMap.get("type");
        if (type == null) return null;
        NodeDeserializer deserializer = this.get(type);
        return deserializer == null ? null : deserializer.decode(safeMap);
    }

    public List<AdventureNode> decodeList(Object list) {
        if (list == null) throw new JsonSyntaxException("Missing field \"nodes\"");
        if (!(list instanceof List<?>)) list = List.of(list);
        List<AdventureNode> nodes = ((List<?>) list).stream().map(this::decode).filter(Objects::nonNull).toList();
        if (nodes.isEmpty()) throw new JsonSyntaxException("Failed to parse field \"nodes\"");
        return nodes;
    }
}
