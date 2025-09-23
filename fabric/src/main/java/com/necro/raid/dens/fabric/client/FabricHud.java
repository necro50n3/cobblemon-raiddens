package com.necro.raid.dens.fabric.client;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.LeaveRaidButton;
import com.necro.raid.dens.common.client.gui.buttons.OverlayButton;
import com.necro.raid.dens.common.network.LeaveRaidPacket;
import com.necro.raid.dens.common.network.RequestResponsePacket;
import com.necro.raid.dens.common.network.RewardResponsePacket;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FabricHud implements HudRenderCallback {
    @Override
    public void onHudRender(GuiGraphics guiGraphics, DeltaTracker delta) {
        if (!RaidDenGuiManager.hasOverlay()) return;

        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        RaidDenGuiManager.render(guiGraphics, width, height);
    }

    public static void init() {
        RaidScreenComponents.LEAVE_RAID_BUTTON = new LeaveRaidButton(button -> {
            NetworkMessages.sendPacketToServer(new LeaveRaidPacket());
            RaidDenGuiManager.RAID_OVERLAY = null;
            button.setFocused(false);
            ((LeaveRaidButton) button).setHover(false);
            Minecraft.getInstance().mouseHandler.grabMouse();
        });

        RaidScreenComponents.ACCEPT_REQUEST_BUTTON = new OverlayButton(
            Component.translatable("screen.cobblemonraiddens.request.accept"),
            button -> {
                NetworkMessages.sendPacketToServer(new RequestResponsePacket(true));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
                button.setFocused(false);
                ((OverlayButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.DENY_REQUEST_BUTTON = new OverlayButton(
            Component.translatable("screen.cobblemonraiddens.request.deny"),
            button -> {
                NetworkMessages.sendPacketToServer(new RequestResponsePacket(false));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
                button.setFocused(false);
                ((OverlayButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );

        RaidScreenComponents.ACCEPT_REWARD_BUTTON = new OverlayButton(
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
                    ((OverlayButton) button).setHover(false);
                    Minecraft.getInstance().mouseHandler.grabMouse();
                }
            }
        );

        RaidScreenComponents.DENY_REWARD_BUTTON = new OverlayButton(
            Component.translatable("screen.cobblemonraiddens.reward.item"),
            button -> {
                NetworkMessages.sendPacketToServer(new RewardResponsePacket(false));
                RaidDenGuiManager.OVERLAY_QUEUE.removeFirst();
                button.setFocused(false);
                ((OverlayButton) button).setHover(false);
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        );
    }
}
