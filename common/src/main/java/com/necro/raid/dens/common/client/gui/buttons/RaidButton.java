package com.necro.raid.dens.common.client.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class RaidButton extends AbstractRaidButton {
    public RaidButton(int width, int height, ResourceLocation texture, ResourceLocation hover, MutableComponent label, Button.OnPress onPress) {
        super(width, height, texture, hover, label, onPress);
    }

    @Override
    protected void render(GuiGraphics guiGraphics, ResourceLocation texture) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.getX(), this.getY(), 4000);
        guiGraphics.blit(texture, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        guiGraphics.drawCenteredString(minecraft.font, this.getMessage(), this.width, 8, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
    }
}
