package com.necro.raid.dens.common.data.adapters;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.util.ComponentUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Type;

public class RaidBossTextAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        return ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }

    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (!json.isJsonPrimitive()) return ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();

        MutableComponent component = (MutableComponent) ComponentSerialization.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
        return ComponentUtils.getRaidBossDefault(component);
    }
}
