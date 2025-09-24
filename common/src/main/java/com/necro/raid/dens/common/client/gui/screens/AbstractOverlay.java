package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public abstract class AbstractOverlay {
    public AbstractOverlay() {}

    public void render(GuiGraphics guiGraphics, int x, int y) {}

    public List<AbstractRaidButton> getButtons() {
        return List.of();
    }
}
