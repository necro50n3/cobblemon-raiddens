package com.necro.raid.dens.common.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.client.ClientRaidRegistry;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.AbstractRaidButton;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.mixins.client.KeyMappingAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public class RaidAdventureOverlay extends AbstractOverlay {
    private static final ResourceLocation OVERLAY = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/overlay.png");
    private static final int WIDTH = 100;
    private static final int HEIGHT = 70;

    private final UUID raid;
    private final ResourceLocation option1;
    private final ResourceLocation option2;

    public RaidAdventureOverlay(UUID raid, ResourceLocation option1, ResourceLocation option2) {
        this.raid = raid;
        this.option1 = option1;
        this.option2 = option2;
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
        guiGraphics.drawCenteredString(font, Component.translatable("screen.cobblemonraiddens.adventure.title").withStyle(ChatFormatting.GOLD), (int) (WIDTH / 1.7), 12, 16777215);
        guiGraphics.pose().popPose();

        this.renderDescription(guiGraphics, x, y);

        if (!RaidDenKeybinds.MOUSE_KEYDOWN.isUnbound()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.5f, 0.5f, 1.0f);
            Component key = getShorthand(((KeyMappingAccessor) RaidDenKeybinds.MOUSE_KEYDOWN).getKey()).copy();
            guiGraphics.drawString(font, Component.translatable("screen.cobblemonraiddens.footer", key), 10, 119, 10197915, false);
            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        RaidScreenComponents.LEFT_ADVENTURE_BUTTON.setPos(x + 4, y + 40);
        RaidScreenComponents.LEFT_ADVENTURE_BUTTON.renderStatic(guiGraphics);

        RaidScreenComponents.RIGHT_ADVENTURE_BUTTON.setPos(x + 51, y + 40);
        RaidScreenComponents.RIGHT_ADVENTURE_BUTTON.renderStatic(guiGraphics);
    }

    private void renderDescription(GuiGraphics guiGraphics, int x, int y) {
        RaidBoss option1 = ClientRaidRegistry.getRaidBoss(this.option1);
        RaidBoss option2 = ClientRaidRegistry.getRaidBoss(this.option2);
        if (option1 == null || option2 == null) return;

        int boxWidth = WIDTH - 8;
        int boxHeight = 32;

        int textureSize = 18;
        int iconY = y + (boxHeight - textureSize) / 2;
        int leftX = x + 4;
        int rightX = x + boxWidth - textureSize - 4;

        guiGraphics.pose().pushPose();
        if (option1.getType() != RaidType.STELLAR) this.renderElementalTypeIcon(guiGraphics, option1.getType());
        else if (ModCompat.MEGA_SHOWDOWN.isLoaded()) this.renderTeraTypeIcon(guiGraphics, option1.getType());
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        if (option2.getType() != RaidType.STELLAR) this.renderElementalTypeIcon(guiGraphics, option2.getType());
        else if (ModCompat.MEGA_SHOWDOWN.isLoaded()) this.renderTeraTypeIcon(guiGraphics, option2.getType());
        guiGraphics.pose().popPose();
    }

    private void renderElementalTypeIcon(GuiGraphics guiGraphics, RaidType type) {
        ResourceLocation texture = ResourceLocation.parse("cobblemon:textures/gui/types_small.png");
        int width = 324;
        int height = 18;
        int offset = getOffset(type, height);

        guiGraphics.blit(texture, 0, 0, offset, 0, height, height, width, height);
        RenderSystem.disableBlend();
    }

    private void renderTeraTypeIcon(GuiGraphics guiGraphics, RaidType type) {
        ResourceLocation texture = ResourceLocation.parse(String.format("mega_showdown:textures/gui/summary/tera_types/%s.png", type.getSerializedName()));
        int width = 32;
        int height = 32;

        guiGraphics.pose().scale(0.75F, 0.75F, 1f);
        guiGraphics.blit(texture, 0, 0, 4, 4, width - 8, height - 8, width, height);
    }

    private static int getOffset(RaidType type, int height) {
        int index = switch (type) {
            case NORMAL -> 0;
            case FIRE -> 1;
            case WATER -> 2;
            case GRASS -> 3;
            case ELECTRIC -> 4;
            case ICE -> 5;
            case FIGHTING -> 6;
            case POISON -> 7;
            case GROUND -> 8;
            case FLYING -> 9;
            case PSYCHIC -> 10;
            case BUG -> 11;
            case ROCK -> 12;
            case GHOST -> 13;
            case DRAGON -> 14;
            case DARK -> 15;
            case STEEL -> 16;
            case FAIRY -> 17;
            default -> 18;
        };

        return height * index;
    }

    @Override
    public List<AbstractRaidButton> getButtons() {
        return List.of(RaidScreenComponents.RIGHT_ADVENTURE_BUTTON, RaidScreenComponents.LEFT_ADVENTURE_BUTTON);
    }

    public UUID raid() {
        return this.raid;
    }

    public ResourceLocation option1() {
        return this.option1;
    }

    public ResourceLocation option2() {
        return this.option2;
    }
}
