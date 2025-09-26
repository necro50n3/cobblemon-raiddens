package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.common.mixins.KeyMappingAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class RaidRequestOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/overlay.png");
    private static final int WIDTH = 100;
    private static final int HEIGHT = 70;

    private final String player;

    public RaidRequestOverlay(String player) {
        this.player = player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY) {
        Font font = Minecraft.getInstance().font;
        int x = (maxX - WIDTH) / 2;
        int y = (int) (maxY * 0.55);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 4000);
        guiGraphics.blit(OVERLAY, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.cobblemonraiddens.request.title").withStyle(ChatFormatting.GOLD), (int) (WIDTH / 1.7), 12, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        Component component = Component.translatable("screen.cobblemonraiddens.request.description", this.player);
        List<FormattedCharSequence> wrapped = font.split(component, WIDTH * 2 - 16);
        int textY = wrapped.size() > 1 ? 44 : 52;
        for (FormattedCharSequence text : wrapped) {
            guiGraphics.drawCenteredString(font, text, WIDTH, textY, 16777215);
            textY += font.lineHeight + 1;
        }
        guiGraphics.pose().popPose();

        if (!RaidDenKeybinds.MOUSE_KEYDOWN.isUnbound()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
            Component key = getShorthand(((KeyMappingAccessor) RaidDenKeybinds.MOUSE_KEYDOWN).getKey()).copy();
            guiGraphics.drawString(font, Component.translatable("screen.cobblemonraiddens.footer", key), 10, 119, 10197915, false);
            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        RaidScreenComponents.ACCEPT_REQUEST_BUTTON.setPos(x + 4, y + 40);
        RaidScreenComponents.ACCEPT_REQUEST_BUTTON.renderStatic(guiGraphics);

        RaidScreenComponents.DENY_REQUEST_BUTTON.setPos(x + 51, y + 40);
        RaidScreenComponents.DENY_REQUEST_BUTTON.renderStatic(guiGraphics);

    }

    @Override
    public List<AbstractRaidButton> getButtons() {
        return List.of(RaidScreenComponents.DENY_REQUEST_BUTTON, RaidScreenComponents.ACCEPT_REQUEST_BUTTON);
    }

    public String getPlayer() {
        return this.player;
    }
}
