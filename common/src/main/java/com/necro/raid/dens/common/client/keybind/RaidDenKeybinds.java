package com.necro.raid.dens.common.client.keybind;

import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.screens.ClickHandler;
import com.necro.raid.dens.common.client.gui.screens.RaidRequestOverlay;
import com.necro.raid.dens.common.client.gui.screens.RaidRewardOverlay;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

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
        if (ACCEPT_SHORTCUT.isDown()) {
            ACCEPT_SHORTCUT.consumeClick();
            if (RaidDenGuiManager.OVERLAY_QUEUE.isEmpty()) RaidDenGuiManager.OVERLAY_QUEUE.add(new RaidRewardOverlay(true));
            else RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
        }

        if (DENY_SHORTCUT.isDown()) {
            DENY_SHORTCUT.consumeClick();
            if (RaidDenGuiManager.OVERLAY_QUEUE.isEmpty()) RaidDenGuiManager.OVERLAY_QUEUE.add(new RaidRequestOverlay("Player 111"));
            else RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
        }

        if (!RaidDenGuiManager.hasOverlay()) return;

        if (MOUSE_KEYDOWN.isDown() && Minecraft.getInstance().screen == null) {
            MOUSE_KEYDOWN.consumeClick();
            Minecraft.getInstance().setScreen(ClickHandler.SCREEN);
        }

        if (!RaidDenGuiManager.hasOverlayQueue()) return;

        if (ACCEPT_SHORTCUT.isDown()) {
            ACCEPT_SHORTCUT.consumeClick();
        }
        else if (DENY_SHORTCUT.isDown()) {
            DENY_SHORTCUT.consumeClick();
        }
    }
}
