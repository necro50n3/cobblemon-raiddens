package com.necro.raid.dens.common.data.adapters;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;

import java.util.ArrayList;
import java.util.List;

public record ScriptAdapter(List<String> functions) {
    public ScriptAdapter(String function) {
        this(List.of(function));
    }

    public static final Codec<ScriptAdapter> CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf())
        .xmap(either -> either.map(
                ScriptAdapter::new,
                ScriptAdapter::new
            ),
            script -> {
                if (script.functions.size() == 1) return Either.left(script.functions.getFirst());
                return Either.right(script.functions);
            }
        );

    public JsonElement serialize() {
        if (this.functions().size() == 1) return new JsonPrimitive(this.functions().getFirst());
        JsonArray array = new JsonArray();
        this.functions().forEach(function -> array.add(new JsonPrimitive(function)));
        return array;
    }

    public static ScriptAdapter deserialize(JsonElement json) {
        switch (json) {
            case JsonPrimitive primitive -> {
                return new ScriptAdapter((String) primitive.getValue());
            }
            case JsonArray array -> {
                List<String> functions = new ArrayList<>();
                for (JsonElement element : array) {
                    if (!(element instanceof JsonPrimitive primitive)) continue;
                    functions.add((String) primitive.getValue());
                }
                return new ScriptAdapter(functions);
            }
            default -> throw new IllegalArgumentException("Expected string or string array for Script, got: " + json);
        }
    }
}
