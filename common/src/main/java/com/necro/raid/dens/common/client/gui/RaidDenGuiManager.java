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
    public static List<AbstractOverlay> PRIORITY_QUEUE = new ArrayList<>();
    public static List<AbstractOverlay> OVERLAY_QUEUE = new ArrayList<>();

    public static void render(GuiGraphics guiGraphics, int maxX, int maxY, float partialTick) {
        if (hasPartyOverlay()) RAID_OVERLAY.render(guiGraphics, maxX, maxY, partialTick);
        if (hasQueue()) getNext().render(guiGraphics, maxX, maxY, partialTick);
    }

    public static List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (hasPartyOverlay()) buttons.addAll(RAID_OVERLAY.getButtons());
        if (hasQueue()) buttons.addAll(getNext().getButtons());
        return buttons;
    }

    public static boolean hasOverlay() {
        return hasPartyOverlay() || hasQueue();
    }

    public static boolean hasQueue() {
        return hasPriorityQueue() || hasOverlayQueue();
    }

    public static boolean hasPartyOverlay() {
        return RAID_OVERLAY != null;
    }

    public static boolean hasPriorityQueue() {
        return !PRIORITY_QUEUE.isEmpty();
    }

    public static boolean hasOverlayQueue() {
        return !OVERLAY_QUEUE.isEmpty();
    }

    public static AbstractOverlay getNext() {
        if (hasPriorityQueue()) return PRIORITY_QUEUE.getFirst();
        else if (hasOverlayQueue()) return OVERLAY_QUEUE.getFirst();
        throw new NullPointerException("No overlay in queue");
    }

    public static void removeNext() {
        if (hasPriorityQueue()) PRIORITY_QUEUE.removeFirst();
        else if (hasOverlayQueue()) OVERLAY_QUEUE.removeFirst();
    }

    public static List<AbstractRaidButton> getOverlayButtons() {
        return hasQueue() ? getNext().getButtons() : List.of();
    }

    public static void tick() {
        if (RAID_OVERLAY != null) RAID_OVERLAY.tick();
    }
}
