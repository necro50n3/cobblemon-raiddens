package com.necro.raid.dens.fabric.events;

import com.necro.raid.dens.common.client.ClientManager;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@SuppressWarnings("unused")
public class ModEvents {
    public static void onPlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = listener.getPlayer();
        if (RaidHelper.isAlreadyHosting(player) || RaidHelper.isAlreadyParticipating(player) || RaidHelper.JOIN_QUEUE.containsKey(player)) {
            RaidDenNetworkMessages.JOIN_RAID.accept(player, true);
        }
        if (RaidHelper.REWARD_QUEUE.containsKey(player.getUUID())) RaidHelper.REWARD_QUEUE.get(player.getUUID()).sendRewardMessage();
    }

    public static void onPlayerDisconnect(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        RaidJoinHelper.onPlayerDisconnect(listener.getPlayer());
        RaidHelper.onPlayerDisconnect(listener.getPlayer());
        DimensionHelper.removeDelayed(server, listener.getPlayer());
    }

    public static void onServerStopping(MinecraftServer server) {
        DimensionHelper.removeDelayed(server);
    }

    public static void initRaidHelper(MinecraftServer server) {
        RaidJoinHelper.initHelper(server);
        RaidHelper.initHelper(server);
    }

    public static void onServerClose(MinecraftServer server) {
        RaidJoinHelper.onServerClose();
    }

    public static void serverTick(MinecraftServer server) {
        RaidJoinHelper.serverTick();
    }

    public static void commonTick(MinecraftServer server) {
        RaidHelper.commonTick();
        DimensionHelper.removePending(server);
    }

    public static void clientTick(Minecraft client) {
        ClientManager.clientTick();
        RaidDenGuiManager.tick();
    }

    public static void initRaidBosses(MinecraftServer server) {
        RaidRegistry.initRaidBosses(server);
        RaidBucketRegistry.init(server);
    }
}
