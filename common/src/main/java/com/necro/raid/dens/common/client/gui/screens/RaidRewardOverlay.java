package com.necro.raid.dens.common.client.gui.screens;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import com.necro.raid.dens.common.mixins.client.KeyMappingAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

public class RaidRewardOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/overlay.png");
    private static final int WIDTH = 100;
    private static final int HEIGHT = 70;

    private final float catchRate;
    private final String pokemon;

    public RaidRewardOverlay(float catchRate, String pokemon) {
        this.catchRate = catchRate;
        this.pokemon = pokemon;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int maxX, int maxY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int x = (int) ((maxX - WIDTH) * CobblemonRaidDensClient.CLIENT_CONFIG.raid_popup_x / 100.0);
        int y = (int) ((maxY - HEIGHT) * CobblemonRaidDensClient.CLIENT_CONFIG.raid_popup_y / 100.0);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 4000);
        guiGraphics.blit(OVERLAY, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.85f, 0.85f, 1.0f);
        guiGraphics.drawCenteredString(font, Component.translatable("screen.cobblemonraiddens.reward.title").withStyle(ChatFormatting.GREEN), (int) (WIDTH / 1.7), 12, 16777215);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
        MutableComponent component;

        if (this.isCatchable()) {
            component = Component.translatable("screen.cobblemonraiddens.reward.description.1", Component.translatable(this.pokemon));

            assert Minecraft.getInstance().player != null;
            float catchRate = this.catchRate;
            ItemStack charm = Minecraft.getInstance().player.getOffhandItem();
            if (charm.is(ModItems.CATCHING_CHARM)) {
                float mod = charm.getOrDefault(ModComponents.CATCH_BOOST.value(), 0F);
                catchRate = Mth.clamp(catchRate * (1F + mod), 0F, 1F);
            }
            int color = this.catchRateColor(catchRate);

            component.append(Component.literal(" (").withColor(color));
            component.append(Component.translatable("jade.cobblemonraiddens.catch_rate", Math.round(catchRate * 100)).withColor(color));
            component.append(Component.literal(")").withColor(color));
        }
        else component = Component.translatable("screen.cobblemonraiddens.reward.description.2", Component.translatable(this.pokemon));
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

        if (this.isCatchable()) {
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
        return this.isCatchable()
            ? List.of(RaidScreenComponents.DENY_REWARD_BUTTON, RaidScreenComponents.ACCEPT_REWARD_BUTTON)
            : List.of(RaidScreenComponents.DENY_WIDE_REWARD_BUTTON);
    }

    private boolean isCatchable() {
        return this.catchRate > 0F;
    }

    private int catchRateColor(float catchRate) {
        float t = Math.max(0f, Math.min(1f, catchRate));

        float hue = 120f * t;
        float saturation = 0.85f;
        float brightness = 0.85f;

        return Color.HSBtoRGB(hue / 360f, saturation, brightness) & 0xFFFFFF;
    }
}
