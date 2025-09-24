package com.necro.raid.dens.common.client.gui.buttons;

import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PopupButton extends AbstractRaidButton {
    private final int index;

    public PopupButton(int width, int height, int index, ResourceLocation texture, ResourceLocation hover, MutableComponent label, OnPress onPress) {
        super(width, height, texture, hover, label, onPress);
        this.index = index;
    }

    @Override
    protected void render(GuiGraphics guiGraphics, ResourceLocation texture) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.getX(), this.getY(), 0f);
        guiGraphics.blit(texture, 0, 0, 0, 0, this.width, this.height, this.width, this.height);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.65f, 0.65f, 1.0f);
        Component component = this.getMessage().copy().append(this.getFormat());
        guiGraphics.drawCenteredString(minecraft.font, component, (int) (this.width / 1.3), 9, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();
    }

    private String getFormat() {
        KeyMapping key = this.index == 0 ? RaidDenKeybinds.ACCEPT_SHORTCUT : RaidDenKeybinds.DENY_SHORTCUT;
        if (key.isUnbound()) return "";
        else return String.format(" [%s]", key.getTranslatedKeyMessage().getString());
    }
}
