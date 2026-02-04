package com.necro.raid.dens.common.compat.wthit;

import com.mojang.blaze3d.systems.RenderSystem;
import mcp.mobius.waila.api.ITooltipComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TeraTypeComponent implements ITooltipComponent {
    private final ResourceLocation texture;
    private final int width;
    private final int height;

    public TeraTypeComponent(ResourceLocation texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return 8;
    }

    @Override
    public int getHeight() {
        return 8;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, DeltaTracker delta) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, this.texture);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(1f / 3f, 1f / 3f, 1f);
        guiGraphics.blit(this.texture, 0, 0, 4, 4, this.width - 8, this.height - 8, this.width, this.height);
        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }
}
