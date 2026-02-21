package com.necro.raid.dens.fabric.events;

import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@SuppressWarnings("unused")
public class ModEvents {
    public static void onPlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = listener.getPlayer();
        server.execute(() -> {
            if (RaidJoinHelper.isParticipatingOrInQueue(player, false)) {
                RaidDenNetworkMessages.JOIN_RAID.accept(player, true);
            }
            if (RaidHelper.REWARD_QUEUE.containsKey(player.getUUID())) RaidHelper.REWARD_QUEUE.get(player.getUUID()).sendRewardMessage(player);
        });
    }

    public static void onPlayerDisconnect(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        ServerPlayer player = listener.getPlayer();
        server.execute(() -> RaidJoinHelper.onPlayerDisconnect(player));
    }

    public static void initRaidHelper(MinecraftServer server) {
        RaidHelper.initHelper(server);
    }

    public static void onServerClose(MinecraftServer server) {
        RaidJoinHelper.onServerClose();
        RaidHelper.onServerClose(server);
    }

    public static void serverTick(MinecraftServer server) {
        RaidJoinHelper.serverTick();
    }

    public static void commonTick(MinecraftServer server) {
        RaidHelper.commonTick();
    }

    public static void clientTick(Minecraft client) {
        RaidDenGuiManager.tick();
    }

    public static void initRaidBosses(MinecraftServer server) {
        RaidBucketRegistry.init(server);
    }
}
