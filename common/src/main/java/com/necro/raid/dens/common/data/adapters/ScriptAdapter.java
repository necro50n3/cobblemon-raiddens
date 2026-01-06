package com.necro.raid.dens.common.data.adapters;

import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;

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
            script -> Either.right(script.functions)
        );

    public JsonElement serialize() {
        try {
            return Jankson.builder().build().loadElement(CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString());
        } catch (SyntaxError e) {
            return new JsonObject();
        }
    }

    public static ScriptAdapter deserialize(JsonElement json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json.toJson())).getOrThrow().getFirst();
    }
}
