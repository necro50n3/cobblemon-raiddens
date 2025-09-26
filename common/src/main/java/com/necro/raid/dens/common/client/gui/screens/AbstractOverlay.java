package com.necro.raid.dens.common.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class AbstractOverlay {
    public AbstractOverlay() {}

    public void render(GuiGraphics guiGraphics, int x, int y) {}

    public List<AbstractRaidButton> getButtons() {
        return List.of();
    }

    protected static Component getShorthand(InputConstants.Key key) {
        return switch (key.getValue()) {
            case InputConstants.KEY_LCONTROL -> Component.translatable("key.cobblemonraiddens.short.left_control");
            case InputConstants.KEY_LALT -> Component.translatable("key.cobblemonraiddens.short.left_alt");
            case InputConstants.KEY_LSHIFT -> Component.translatable("key.cobblemonraiddens.short.left_shift");
            case InputConstants.KEY_RCONTROL -> Component.translatable("key.cobblemonraiddens.short.right_control");
            case InputConstants.KEY_RALT -> Component.translatable("key.cobblemonraiddens.short.right_alt");
            case InputConstants.KEY_RSHIFT -> Component.translatable("key.cobblemonraiddens.short.right_shift");
            case InputConstants.MOUSE_BUTTON_LEFT -> Component.translatable("key.cobblemonraiddens.short.left_mouse");
            case InputConstants.MOUSE_BUTTON_MIDDLE -> Component.translatable("key.cobblemonraiddens.short.middle_mouse");
            case InputConstants.MOUSE_BUTTON_RIGHT -> Component.translatable("key.cobblemonraiddens.short.right_mouse");
            default -> key.getDisplayName();
        };
    }
}
