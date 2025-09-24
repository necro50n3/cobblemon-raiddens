package com.necro.raid.dens.common.client.gui.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractRaidButton extends Button {
    private final ResourceLocation texture;
    private final ResourceLocation hover;

    public AbstractRaidButton(int width, int height, ResourceLocation texture, ResourceLocation hover, MutableComponent label, OnPress onPress) {
        super(0, 0, width, height, label, onPress, DEFAULT_NARRATION);
        this.texture = texture;
        this.hover = hover;
    }

    public void renderStatic(GuiGraphics guiGraphics) {
        this.render(guiGraphics, this.texture);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        this.render(guiGraphics, this.isHoveredOrFocused() ? this.hover : this.texture);
    }

    protected abstract void render(GuiGraphics guiGraphics, ResourceLocation texture);

    public void setHover(boolean isHovered) {
        this.isHovered = isHovered;
    }

    public void setPos(int x, int y) {
        this.setX(x);
        this.setY(y);
    }
}
