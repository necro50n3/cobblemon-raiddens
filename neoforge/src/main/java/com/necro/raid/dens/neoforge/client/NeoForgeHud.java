package com.necro.raid.dens.neoforge.client;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.PopupButton;
import com.necro.raid.dens.common.client.gui.buttons.RaidButton;
import com.necro.raid.dens.common.client.gui.screens.RaidRequestOverlay;
import com.necro.raid.dens.common.network.LeaveRaidPacket;
import com.necro.raid.dens.common.network.RequestResponsePacket;
import com.necro.raid.dens.common.network.RewardResponsePacket;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class NeoForgeHud {
    public static void init() {
        RaidScreenComponents.LEAVE_RAID_BUTTON = new RaidButton(
            32, 12,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/leave_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/leave_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.raid.button"),
            button -> {
                NetworkMessages.sendPacketToServer(new LeaveRaidPacket());
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
                if (RaidDenGuiManager.OVERLAY_QUEUE.isEmpty() || !(RaidDenGuiManager.OVERLAY_QUEUE.getFirst() instanceof RaidRequestOverlay overlay)) return;
                String player = overlay.getPlayer();
                NetworkMessages.sendPacketToServer(new RequestResponsePacket(true, player));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
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
                if (RaidDenGuiManager.OVERLAY_QUEUE.isEmpty() || !(RaidDenGuiManager.OVERLAY_QUEUE.getFirst() instanceof RaidRequestOverlay overlay)) return;
                String player = overlay.getPlayer();
                NetworkMessages.sendPacketToServer(new RequestResponsePacket(false, player));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
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
                LocalPlayer player = Minecraft.getInstance().player;
                assert player != null;
                ItemStack stack = player.getMainHandItem();
                if (!(stack.getItem() instanceof PokeBallItem)) {
                    player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.reward.reward_not_pokeball"));
                }
                else {
                    NetworkMessages.sendPacketToServer(new RewardResponsePacket(true));
                    RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
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
                NetworkMessages.sendPacketToServer(new RewardResponsePacket(false));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
                button.setFocused(false);
                ((PopupButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.DENY_WIDE_REWARD_BUTTON = new PopupButton(
            92, 16, 1,
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/wide_button.png"),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/popup/wide_button_hover.png"),
            Component.translatable("screen.cobblemonraiddens.reward.item"),
            button -> {
                NetworkMessages.sendPacketToServer(new RewardResponsePacket(false));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
                button.setFocused(false);
                ((PopupButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );
    }
}
