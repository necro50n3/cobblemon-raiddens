package com.necro.raid.dens.common.client.gui.screens;

import com.mojang.blaze3d.platform.InputConstants;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.common.mixins.KeyMappingAccessor;
import net.minecraft.client.Minecraft;
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
        if (!isKeyDown(((KeyMappingAccessor) RaidDenKeybinds.MOUSE_KEYDOWN).getKey().getValue()) && this.minecraft != null)
            this.minecraft.mouseHandler.grabMouse();
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }

    @Override
    public void afterKeyboardAction() {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
