package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.common.mixins.client.KeyMappingAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class RaidRewardOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/overlay.png");
    private static final int WIDTH = 100;
    private static final int HEIGHT = 70;

    private final boolean isCatchable;
    private final String pokemon;

    public RaidRewardOverlay(boolean isCatchable, String pokemon) {
        this.isCatchable = isCatchable;
        this.pokemon = pokemon;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int x = (maxX - WIDTH) / 2;
        int y = (int) (maxY * 0.55);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 4000);
        guiGraphics.blit(OVERLAY, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.cobblemonraiddens.reward.title").withStyle(ChatFormatting.GREEN), (int) (WIDTH / 1.7), 12, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        MutableComponent component = this.isCatchable
            ? Component.translatable("screen.cobblemonraiddens.reward.description.1", Component.translatable(this.pokemon))
            : Component.translatable("screen.cobblemonraiddens.reward.description.2", Component.translatable(this.pokemon));
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
            Component key = getShorthand(((KeyMappingAccessor) RaidDenKeybinds.MOUSE_KEYDOWN).getKey());
            guiGraphics.drawString(font, Component.translatable("screen.cobblemonraiddens.footer", key), 10, 119, 10197915, false);
            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        if (this.isCatchable) {
            RaidScreenComponents.ACCEPT_REWARD_BUTTON.setPos(x + 4, y + 40);
            RaidScreenComponents.ACCEPT_REWARD_BUTTON.renderStatic(guiGraphics);

            RaidScreenComponents.DENY_REWARD_BUTTON.setPos(x + 51, y + 40);
            RaidScreenComponents.DENY_REWARD_BUTTON.renderStatic(guiGraphics);
        }
        else {
            RaidScreenComponents.DENY_WIDE_REWARD_BUTTON.setPos(x + 4, y + 40);
            RaidScreenComponents.DENY_WIDE_REWARD_BUTTON.renderStatic(guiGraphics);
        }
    }

    @Override
    public List<AbstractRaidButton> getButtons() {
        return this.isCatchable
            ? List.of(RaidScreenComponents.DENY_REWARD_BUTTON, RaidScreenComponents.ACCEPT_REWARD_BUTTON)
            : List.of(RaidScreenComponents.DENY_WIDE_REWARD_BUTTON);
    }
}
