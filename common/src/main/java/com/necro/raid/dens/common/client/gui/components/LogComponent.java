package com.necro.raid.dens.common.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

public class LogComponent {
    private static final ResourceLocation LOG = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/log.png");
    private static final int LOG_WIDTH = 90;
    private static final int LOG_HEIGHT = 16;

    private final Component log;
    private int tick;

    public LogComponent(Component log) {
        this.log = ((MutableComponent) log).withStyle(ChatFormatting.ITALIC);
    }

    public void render(GuiGraphics guiGraphics, int x, int y, float partialTick) {
        Font font = Minecraft.getInstance().font;
        float rate = Math.min((this.tick + partialTick) / 6f, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, (float) this.getOpacity(0.5, partialTick));
        guiGraphics.blit(LOG, (int) (-LOG_WIDTH + rate * (x + LOG_WIDTH)), y, 0, 0, LOG_WIDTH, LOG_HEIGHT, LOG_WIDTH, LOG_HEIGHT);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x - ((1 - rate) * LOG_WIDTH), y, 0);
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);

        List<FormattedCharSequence> wrapped = font.split(this.log, LOG_WIDTH * 2 - 30);
        int textY = wrapped.size() > 1 ? 7 : 12;
        for (FormattedCharSequence text : wrapped) {
            guiGraphics.drawString(font, text, 6, textY, this.getTextColor(partialTick));
            textY += font.lineHeight + 1;
        }

        guiGraphics.pose().popPose();
    }

    private int getTextColor(float partialTick) {
        double opacity = this.getOpacity(1.0, partialTick);
        if (opacity == 0) return 0;
        int alpha = (int) Math.round(opacity * 255);
        return (alpha << 24) | (0xFFFFFF);
    }

    private double getOpacity(double baseOpacity, float partialTick) {
        double opacity;
        double tick = this.tick + partialTick;

        if (tick == 0) return 0;
        else if (tick < 300) opacity = baseOpacity;
        else if (tick <= 310) {
            double fadeProgress = (tick - 300) / 10.0;
            opacity = baseOpacity * (1.0 - fadeProgress);
        }
        else return 0;

        opacity = Mth.clamp( opacity, 0.0, 1.0);
        return opacity;
    }

    public boolean tick() {
        return this.tick++ > 310;
    }
}
