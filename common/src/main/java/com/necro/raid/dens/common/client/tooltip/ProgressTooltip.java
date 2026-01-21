package com.necro.raid.dens.common.client.tooltip;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public record ProgressTooltip(double progress) implements ClientTooltipComponent {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/tooltip/progress_bar.png");

    @Override
    public int getWidth(@NotNull Font font) {
        return 101;
    }

    @Override
    public int getHeight() {
        return 5;
    }

    @Override
    public void renderImage(@NotNull Font font, int i, int j, @NotNull GuiGraphics guiGraphics) {
        double progress = Mth.clamp(this.progress, 0.0, 1.0);
        int filledWidth = (int) (101 * progress);

        guiGraphics.blit(TEXTURE, i, j, 0, 5, 101, 5, 101, 10);
        guiGraphics.blit(TEXTURE, i, j, 0, 0, filledWidth, 5, 101, 10);
    }
}
