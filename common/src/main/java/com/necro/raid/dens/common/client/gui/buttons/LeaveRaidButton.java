package com.necro.raid.dens.common.client.gui.buttons;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LeaveRaidButton extends Button {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/leave_button.png");
    private static final ResourceLocation HOVER = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/leave_button_hover.png");

    public LeaveRaidButton(OnPress onPress) {
        super(0, 0, 32, 12, Component.translatable("screen.cobblemonraiddens.raid.button").withStyle(ChatFormatting.BOLD), onPress, DEFAULT_NARRATION);
    }

    public void renderStatic(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.getX(), this.getY(), 0f);
        guiGraphics.blit(TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        guiGraphics.drawCenteredString(minecraft.font, this.getMessage(), this.width, 8, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.getX(), this.getY(), 0f);
        guiGraphics.blit(this.isHoveredOrFocused() ? HOVER : TEXTURE, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        guiGraphics.drawCenteredString(minecraft.font, this.getMessage(), this.width, 8, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
    }

    public void setHover(boolean isHovered) {
        this.isHovered = isHovered;
    }
}
