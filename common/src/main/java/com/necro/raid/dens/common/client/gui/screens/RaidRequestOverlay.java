package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class RaidRequestOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/overlay.png");
    private static final int WIDTH = 100;
    private static final int HEIGHT = 60;

    private final String player;

    public RaidRequestOverlay(String player) {
        this.player = player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY) {
        Font font = Minecraft.getInstance().font;
        int x = (maxX - WIDTH) / 2;
        int y = (int) (maxY * 0.6);

        guiGraphics.blit(OVERLAY, x, y, 0, 0, WIDTH,HEIGHT, WIDTH,HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.cobblemonraiddens.request.title"), (int) (WIDTH / 1.7), 12, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);

        Component component = Component.translatable("screen.cobblemonraiddens.request.description", this.player);
        List<FormattedCharSequence> wrapped = font.split(component, WIDTH * 2 - 16);
        int textY = 44;
        for (FormattedCharSequence text : wrapped) {
            guiGraphics.drawCenteredString(font, text, WIDTH, textY, 16777215);
            textY += font.lineHeight + 1;
        }

        guiGraphics.pose().popPose();

        guiGraphics.pose().popPose();

        RaidScreenComponents.ACCEPT_REQUEST_BUTTON.setPos(x + 4, y + 40);
        RaidScreenComponents.ACCEPT_REQUEST_BUTTON.renderStatic(guiGraphics);

        RaidScreenComponents.DENY_REQUEST_BUTTON.setPos(x + 51, y + 40);
        RaidScreenComponents.DENY_REQUEST_BUTTON.renderStatic(guiGraphics);
    }

    @Override
    public List<Button> getButtons() {
        return List.of(RaidScreenComponents.ACCEPT_REQUEST_BUTTON, RaidScreenComponents.DENY_REQUEST_BUTTON);
    }
}
