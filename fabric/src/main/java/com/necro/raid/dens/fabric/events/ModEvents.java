package com.necro.raid.dens.fabric.events;

import com.necro.raid.dens.common.client.ClientManager;
import com.necro.raid.dens.common.network.JoinRaidPacket;
import com.necro.raid.dens.common.network.RequestPacket;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
import com.necro.raid.dens.common.util.RaidRegistry;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ModEvents {
    public static void onPlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = listener.getPlayer();
        if (RaidHelper.isAlreadyHosting(player) || RaidHelper.isAlreadyParticipating(player) || RaidHelper.JOIN_QUEUE.containsKey(player)) {
            NetworkMessages.sendPacketToPlayer(player, new JoinRaidPacket(true));
        }
        if (RaidHelper.REWARD_QUEUE.containsKey(player)) RaidHelper.REWARD_QUEUE.get(player).sendRewardMessage();

        NetworkMessages.sendPacketToPlayer(player, new RequestPacket("Player"));
    }

    public static void onPlayerDisconnect(ServerGamePacketListenerImpl listener, MinecraftServer minecraftServer) {
        RaidHelper.onPlayerDisconnect(listener.getPlayer());
    }

    public static void initRaidHelper(MinecraftServer server) {
        RaidHelper.initHelper(server);
    }

    public static void onServerClose(MinecraftServer server) {
        RaidHelper.onServerClose();
    }

    public static void serverTick(MinecraftServer server) {
        RaidHelper.serverTick();
    }

    public static void commonTick(MinecraftServer server) {
        RaidHelper.commonTick();
    }

    public static void clientTick(Minecraft client) {
        ClientManager.clientTick();
    }

    public static void initRaidBosses(MinecraftServer server) {
        RaidRegistry.initRaidBosses(server);
        RaidBucketRegistry.init(server);
    }
}
