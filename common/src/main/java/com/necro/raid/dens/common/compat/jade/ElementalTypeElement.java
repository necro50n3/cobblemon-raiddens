package com.necro.raid.dens.common.compat.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

public class ElementalTypeElement extends Element {
    private final ResourceLocation texture;
    private final int width;
    private final int height;
    private final int index;

    public ElementalTypeElement(ResourceLocation texture, int width, int height, RaidType type) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.index = switch (type) {
            case NORMAL -> 0;
            case FIRE -> 1;
            case WATER -> 2;
            case GRASS -> 3;
            case ELECTRIC -> 4;
            case ICE -> 5;
            case FIGHTING -> 6;
            case POISON -> 7;
            case GROUND -> 8;
            case FLYING -> 9;
            case PSYCHIC -> 10;
            case BUG -> 11;
            case ROCK -> 12;
            case GHOST -> 13;
            case DRAGON -> 14;
            case DARK -> 15;
            case STEEL -> 16;
            case FAIRY -> 17;
            default -> 18;
        };
    }

    @Override
    public Vec2 getSize() {
        return new Vec2(8f, 8f);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        int offset = this.height * this.index;
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, this.texture);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(4f / 9f, 4f / 9f, 1f);
        guiGraphics.blit(this.texture, 0, 0, offset, 0, this.height, this.height, this.width, this.height);
        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }
}
