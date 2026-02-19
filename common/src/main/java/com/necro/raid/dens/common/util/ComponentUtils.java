package com.necro.raid.dens.common.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

public class ComponentUtils {
    public static Component getSystemMessage(String translatable) {
        return getSystemMessage(Component.translatable(translatable));
    }

    public static Component getSystemMessage(MutableComponent component) {
        return component;
    }
    public static Component getErrorMessage(String translatable) {
        return getErrorMessage(Component.translatable(translatable));
    }

    public static Component getErrorMessage(MutableComponent component) {
        return component.withStyle(ChatFormatting.RED);
    }

    public static Component fromJsonText(String jsonString) {
        return ComponentSerialization.CODEC
		.decode(JsonOps.INSTANCE, new Gson().fromJson(jsonString, JsonElement.class))
		.getOrThrow().
		.getFirst();
    }
}
