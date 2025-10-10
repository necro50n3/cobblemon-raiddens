package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.gui.components.LogComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RaidOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/overlay.png");
    private static final int OVERLAY_WIDTH = 40;
    private static final int OVERLAY_HEIGHT = 34;

    private final List<LogComponent> battleLog;

    public RaidOverlay() {
        this.battleLog = new ArrayList<>();
    }

    public void addLog(Component log) {
        if (this.battleLog.size() > 3) this.battleLog.removeFirst();
        this.battleLog.add(new LogComponent(log));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int x = maxX - OVERLAY_WIDTH;
        int y = (int) (maxY * 0.4);

        guiGraphics.blit(OVERLAY, x, y, 0, 0, OVERLAY_WIDTH, OVERLAY_HEIGHT, OVERLAY_WIDTH, OVERLAY_HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);

        Component component = Component.translatable("screen.cobblemonraiddens.raid.title");
        List<FormattedCharSequence> wrapped = font.split(component, OVERLAY_WIDTH * 2 - 6);
        int textY = 12;
        for (FormattedCharSequence text : wrapped) {
            guiGraphics.drawCenteredString(font, text, OVERLAY_WIDTH, textY, 16777215);
            textY += 12;
        }

        guiGraphics.pose().popPose();

        if (CobblemonRaidDensClient.CLIENT_CONFIG.enable_raid_logs) {
            int logX = 0;
            int logY = (int) (maxY * 0.3);
            for (int i = 0; i < this.battleLog.size(); i++) {
                this.battleLog.get(i).render(guiGraphics, logX, logY + (i * 18), partialTick);
            }
        }

        RaidScreenComponents.LEAVE_RAID_BUTTON.setX(x + 4);
        RaidScreenComponents.LEAVE_RAID_BUTTON.setY(y + 18);
        RaidScreenComponents.LEAVE_RAID_BUTTON.renderStatic(guiGraphics);
    }

    @Override
    public List<AbstractRaidButton> getButtons() {
        return List.of(RaidScreenComponents.LEAVE_RAID_BUTTON);
    }

    public void tick() {
        this.battleLog.removeIf(LogComponent::tick);
    }
}
