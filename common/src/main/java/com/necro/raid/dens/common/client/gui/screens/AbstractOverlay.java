package com.necro.raid.dens.common.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

import java.util.List;

public abstract class AbstractOverlay {
    public AbstractOverlay() {}

    public void render(GuiGraphics guiGraphics, int x, int y) {}

    public List<Button> getButtons() {
        return List.of();
    }
}
