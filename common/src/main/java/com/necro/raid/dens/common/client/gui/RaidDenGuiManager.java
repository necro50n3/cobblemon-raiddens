package com.necro.raid.dens.common.client.gui;

import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.gui.screens.AbstractOverlay;
import com.necro.raid.dens.common.client.gui.screens.RaidOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

import java.util.ArrayList;
import java.util.List;

public class RaidDenGuiManager {
    public static RaidOverlay RAID_OVERLAY = null;
    public static List<AbstractOverlay> OVERLAY_QUEUE = new ArrayList<>();

    public static void render(GuiGraphics guiGraphics, int maxX, int maxY, float partialTick) {
        if (hasPartyOverlay()) RAID_OVERLAY.render(guiGraphics, maxX, maxY, partialTick);
        if (hasOverlayQueue()) OVERLAY_QUEUE.getFirst().render(guiGraphics, maxX, maxY, partialTick);
    }

    public static List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (hasPartyOverlay()) buttons.addAll(RAID_OVERLAY.getButtons());
        if (hasOverlayQueue()) buttons.addAll(OVERLAY_QUEUE.getFirst().getButtons());
        return buttons;
    }

    public static boolean hasOverlay() {
        return hasPartyOverlay() || hasOverlayQueue();
    }

    public static boolean hasPartyOverlay() {
        return RAID_OVERLAY != null;
    }

    public static boolean hasOverlayQueue() {
        return !OVERLAY_QUEUE.isEmpty();
    }

    public static List<AbstractRaidButton> getOverlayButtons() {
        if (OVERLAY_QUEUE.isEmpty()) return List.of();
        else return OVERLAY_QUEUE.getFirst().getButtons();
    }

    public static void tick() {
        if (RAID_OVERLAY != null) RAID_OVERLAY.tick();
    }
}
