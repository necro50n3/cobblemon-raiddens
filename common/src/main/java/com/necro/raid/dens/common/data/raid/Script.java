package com.necro.raid.dens.common.data.raid;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonElement;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.JsonObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;

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
            script -> Either.right(script.functions)
        );

    public JsonElement serialize() {
        try {
            return Jankson.builder().build().loadElement(CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString());
        } catch (SyntaxError e) {
            return new JsonObject();
        }
    }

    public static Script deserialize(JsonElement json) {
        return CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(json.toJson())).getOrThrow().getFirst();
    }
}
