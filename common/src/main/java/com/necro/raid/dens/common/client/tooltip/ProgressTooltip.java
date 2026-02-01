package com.necro.raid.dens.common.client.tooltip;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public record ProgressTooltip(double progress, double total) implements ClientTooltipComponent {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/tooltip/progress_bar.png");

    @Override
    public int getWidth(@NotNull Font font) {
        return 101;
    }

    @Override
    public int getHeight() {
        return Screen.hasShiftDown() ? 18 : 10;
    }

    @Override
    public void renderText(@NotNull Font font, int i, int j, @NotNull Matrix4f matrix, MultiBufferSource.@NotNull BufferSource buffer) {
        if (!Screen.hasShiftDown()) return;
        font.drawInBatch(String.format("%d / %d", (int) this.progress, (int) this.total), i, j + 8, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    public void renderImage(@NotNull Font font, int i, int j, @NotNull GuiGraphics guiGraphics) {
        double progress = Mth.clamp(this.progress / this.total, 0.0, 1.0);
        int filledWidth = (int) (101 * progress);

        guiGraphics.blit(TEXTURE, i, j, 0, 5, 101, 5, 101, 10);
        guiGraphics.blit(TEXTURE, i, j, 0, 0, filledWidth, 5, 101, 10);
    }
}
