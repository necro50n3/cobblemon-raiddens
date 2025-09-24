package com.necro.raid.dens.common.client.gui.buttons;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PopupButton extends AbstractRaidButton {
    public PopupButton(int width, int height, ResourceLocation texture, ResourceLocation hover, MutableComponent label, OnPress onPress) {
        super(width, height, texture, hover, label, onPress);
    }

    @Override
    protected void render(GuiGraphics guiGraphics, ResourceLocation texture) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.getX(), this.getY(), 0f);
        guiGraphics.blit(texture, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
        guiGraphics.drawCenteredString(minecraft.font, this.getMessage(), (int) (this.width / 1.7), 6, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
    }
}
