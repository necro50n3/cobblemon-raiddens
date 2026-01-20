package com.necro.raid.dens.common.data.raid;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonArray;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public record Script(List<String> functions) {
    public Script(String function) {
        this(List.of(function));
    }

    public static final Codec<Script> CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf())
        .xmap(either -> either.map(
                Script::new,
                Script::new
            ),
            script -> {
                if (script.functions.size() == 1) return Either.left(script.functions.getFirst());
                return Either.right(script.functions);
            }
        );

    public JsonElement serialize() {
        if (this.functions.size() == 1) return new JsonPrimitive(this.functions.getFirst());
        JsonArray array = new JsonArray();
        this.functions.forEach(function -> array.add(new JsonPrimitive(function)));
        return array;
    }

    public static Script deserialize(JsonElement json) {
        switch (json) {
            case JsonPrimitive primitive -> {
                return new Script((String) primitive.getValue());
            }
            case JsonArray array -> {
                List<String> functions = new ArrayList<>();
                for (JsonElement element : array) {
                    if (!(element instanceof JsonPrimitive primitive)) continue;
                    functions.add((String) primitive.getValue());
                }
                return new Script(functions);
            }
            default -> throw new IllegalArgumentException("Expected string or string array for Script, got: " + json);
        }
    }
}
