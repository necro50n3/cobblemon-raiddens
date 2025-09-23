package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RaidOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/overlay.png");

    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY) {
        int x = maxX - 40;
        int y = (int) (maxY * 0.4);

        guiGraphics.blit(OVERLAY, x, y, 0, 0, 40,34, 40,34);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0f);
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("screen.cobblemonraiddens.raid.title.1"), 40, 12, 16777215);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("screen.cobblemonraiddens.raid.title.2"), 40, 24, 16777215);
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
