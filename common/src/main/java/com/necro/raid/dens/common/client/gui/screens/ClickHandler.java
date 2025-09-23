package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickHandler extends Screen {
    public static final ClickHandler SCREEN = new ClickHandler();

    private ClickHandler() { super(Component.literal("")); }

    @Override
    protected void init() {
        RaidDenGuiManager.getButtons().forEach(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        super.render(guiGraphics, x, y, partialTick);
    }

    @Override
    protected void renderBlurredBackground(float f) {}

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics, int i, int j, int k, int l) {}

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {}

    @Override
    public void tick() {
        if (!hasAltDown() && this.minecraft != null) this.minecraft.mouseHandler.grabMouse();
    }

    @Override
    public void afterKeyboardAction() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
