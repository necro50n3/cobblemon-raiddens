package com.necro.raid.dens.common.client;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.AdventureButton;
import com.necro.raid.dens.common.client.gui.buttons.PopupButton;
import com.necro.raid.dens.common.client.gui.buttons.RaidButton;
import com.necro.raid.dens.common.client.gui.screens.RaidAdventureOverlay;
import com.necro.raid.dens.common.client.gui.screens.RaidRequestOverlay;
import com.necro.raid.dens.common.client.gui.screens.RaidRewardOverlay;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ClientHud {
    public static void init() {
        RaidScreenComponents.LEAVE_RAID_BUTTON = new RaidButton(
            32, 12,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/leave_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/leave_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.raid.button"),
            button -> {
                RaidDenNetworkMessages.LEAVE_RAID.run();
                RaidDenGuiManager.RAID_OVERLAY = null;
                button.setFocused(false);
                ((RaidButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.ACCEPT_REQUEST_BUTTON = new PopupButton(
            45, 16, 0,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/accept_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/accept_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.request.accept"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidRequestOverlay overlay)) return;
                String player = overlay.getPlayer();
                RaidDenNetworkMessages.REQUEST_RESPONSE.accept(true, player);
                RaidDenGuiManager.removeNext();
                button.setFocused(false);
                ((PopupButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.DENY_REQUEST_BUTTON = new PopupButton(
            45, 16, 1,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/deny_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/deny_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.request.deny"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidRequestOverlay overlay)) return;
                String player = overlay.getPlayer();
                RaidDenNetworkMessages.REQUEST_RESPONSE.accept(false, player);
                RaidDenGuiManager.removeNext();
                button.setFocused(false);
                ((PopupButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.ACCEPT_REWARD_BUTTON = new PopupButton(
            45, 16, 0,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/accept_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/accept_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.reward.pokemon"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidRewardOverlay)) return;
                LocalPlayer player = Minecraft.getInstance().player;
                assert player != null;
                ItemStack stack = player.getMainHandItem();
                if (!(stack.getItem() instanceof PokeBallItem)) {
                    player.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"));
                }
                else {
                    RaidDenNetworkMessages.REWARD_RESPONSE.accept(true);
                    RaidDenGuiManager.removeNext();
                    button.setFocused(false);
                    ((PopupButton) button).setHover(false);
                    Minecraft.getInstance().mouseHandler.grabMouse();
                }
            }
        );

        RaidScreenComponents.DENY_REWARD_BUTTON = new PopupButton(
            45, 16, 1,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/deny_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/deny_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.reward.item"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidRewardOverlay)) return;
                RaidDenNetworkMessages.REWARD_RESPONSE.accept(false);
                RaidDenGuiManager.removeNext();
                button.setFocused(false);
                ((PopupButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.DENY_WIDE_REWARD_BUTTON = new PopupButton(
            92, 16, 1,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/wide_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/wide_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.reward.force_item"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidRewardOverlay)) return;
                RaidDenNetworkMessages.REWARD_RESPONSE.accept(false);
                RaidDenGuiManager.removeNext();
                button.setFocused(false);
                ((PopupButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.LEFT_ADVENTURE_BUTTON = new AdventureButton(
            45, 16,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/adventure/left_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/adventure/left_button_hover.png"),
            Component.literal("<"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidAdventureOverlay overlay)) return;
                RaidDenNetworkMessages.CHOOSE_RAID_BOSS_RESPONSE.accept(overlay.raid(), overlay.option1());
                RaidDenGuiManager.removeNext();
                button.setFocused(false);
                ((AdventureButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.RIGHT_ADVENTURE_BUTTON = new AdventureButton(
            45, 16,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/adventure/right_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/adventure/right_button_hover.png"),
            Component.literal(">"),
            button -> {
                if (!RaidDenGuiManager.hasQueue() || !(RaidDenGuiManager.getNext() instanceof RaidAdventureOverlay overlay)) return;
                RaidDenNetworkMessages.CHOOSE_RAID_BOSS_RESPONSE.accept(overlay.raid(), overlay.option2());
                RaidDenGuiManager.removeNext();
                button.setFocused(false);
                ((AdventureButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );
    }
}
