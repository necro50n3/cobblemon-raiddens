package com.necro.raid.dens.neoforge.client;

import com.cobblemon.mod.common.item.PokeBallItem;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import com.necro.raid.dens.common.client.gui.buttons.LeaveRaidButton;
import com.necro.raid.dens.common.client.gui.buttons.OverlayButton;
import com.necro.raid.dens.common.network.LeaveRaidPacket;
import com.necro.raid.dens.common.network.RequestResponsePacket;
import com.necro.raid.dens.common.network.RewardResponsePacket;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class NeoForgeHud {
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
