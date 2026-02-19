package com.necro.raid.dens.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

    public static Component getRaidBossDefault(MutableComponent component) {
        return component.withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD);
    }
}
