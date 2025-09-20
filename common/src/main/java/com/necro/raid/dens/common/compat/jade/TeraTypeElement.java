package com.necro.raid.dens.common.compat.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

public class TeraTypeElement extends Element {
    private final ResourceLocation texture;
    private final int width;
    private final int height;

    public TeraTypeElement(ResourceLocation texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(8f, 8f);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
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
