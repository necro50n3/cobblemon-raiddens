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

public class RaidOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/overlay.png");
    private static final int WIDTH = 40;
    private static final int HEIGHT = 34;


    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY) {
        Font font = Minecraft.getInstance().font;
        int x = maxX - WIDTH;
        int y = (int) (maxY * 0.4);

        guiGraphics.blit(OVERLAY, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);

        Component component = Component.translatable("screen.cobblemonraiddens.raid.title");
        List<FormattedCharSequence> wrapped = font.split(component, WIDTH * 2 - 6);
        int textY = 12;
        for (FormattedCharSequence text : wrapped) {
            guiGraphics.drawCenteredString(font, text, WIDTH, textY, 16777215);
            textY += 12;
        }

        guiGraphics.pose().popPose();

        RaidScreenComponents.LEAVE_RAID_BUTTON.setX(x + 4);
        RaidScreenComponents.LEAVE_RAID_BUTTON.setY(y + 18);
        RaidScreenComponents.LEAVE_RAID_BUTTON.renderStatic(guiGraphics);
    }

    @Override
    public List<Button> getButtons() {
        return List.of(RaidScreenComponents.LEAVE_RAID_BUTTON);
    }
}
