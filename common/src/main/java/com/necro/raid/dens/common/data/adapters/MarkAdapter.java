package com.necro.raid.dens.common.data.adapters;

import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.mark.Marks;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class MarkAdapter implements JsonSerializer<Mark>, JsonDeserializer<Mark> {
    private static final Codec<Mark> CODEC = ResourceLocation.CODEC.xmap(Marks::getByIdentifier, Mark::getIdentifier);

    @Override
    public JsonElement serialize(Mark src, Type typeOfSrc, JsonSerializationContext context) {
        return CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public Mark deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }
}
