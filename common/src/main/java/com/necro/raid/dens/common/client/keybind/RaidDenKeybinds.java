package com.necro.raid.dens.common.client.keybind;

import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.gui.screens.ClickHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class RaidDenKeybinds {
    public static final KeyMapping MOUSE_KEYDOWN = new KeyMapping(
        "key.cobblemonraiddens.mouse", GLFW.GLFW_KEY_LEFT_ALT, "category.cobblemonraiddens.raid_dens"
    );

    public static final KeyMapping ACCEPT_SHORTCUT = new KeyMapping(
        "key.cobblemonraiddens.accept", GLFW.GLFW_KEY_1, "category.cobblemonraiddens.raid_dens"
    );

    public static final KeyMapping DENY_SHORTCUT = new KeyMapping(
        "key.cobblemonraiddens.deny", GLFW.GLFW_KEY_2, "category.cobblemonraiddens.raid_dens"
    );

    public static void handleKeyInput() {
        if (!RaidDenGuiManager.hasOverlay()) return;

        if (MOUSE_KEYDOWN.isDown() && Minecraft.getInstance().screen == null) {
            MOUSE_KEYDOWN.consumeClick();
            Minecraft.getInstance().setScreen(ClickHandler.SCREEN);
        }

        if (!RaidDenGuiManager.hasOverlayQueue()) return;

        if (ACCEPT_SHORTCUT.isDown()) {
            ACCEPT_SHORTCUT.consumeClick();
            List<AbstractRaidButton> buttons = RaidDenGuiManager.OVERLAY_QUEUE.getFirst().getButtons();
            if (buttons.isEmpty()) return;
            buttons.getFirst().onPress();
        }
        else if (DENY_SHORTCUT.isDown()) {
            DENY_SHORTCUT.consumeClick();
            List<AbstractRaidButton> buttons = RaidDenGuiManager.OVERLAY_QUEUE.getFirst().getButtons();
            if (buttons.size() < 2) return;
            buttons.get(1).onPress();
        }
    }
}
