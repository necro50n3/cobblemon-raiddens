package com.necro.raid.dens.fabric.client;

import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FabricHud implements HudRenderCallback {
    @Override
    public void onHudRender(GuiGraphics guiGraphics, DeltaTracker delta) {
        if (!RaidDenGuiManager.hasOverlay()) return;

        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        RaidDenGuiManager.render(guiGraphics, width, height, delta.getGameTimeDeltaTicks());
    }
}
