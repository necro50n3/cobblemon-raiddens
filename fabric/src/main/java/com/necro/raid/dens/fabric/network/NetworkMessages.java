package com.necro.raid.dens.fabric.network;

import com.necro.raid.dens.common.network.RaidChallengePacket;
import com.necro.raid.dens.common.network.SyncHealthPacket;
import com.necro.raid.dens.common.network.SyncRaidDimensionsPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class NetworkMessages {
    public static void registerPayload() {
        PayloadTypeRegistry.playS2C().register(SyncRaidDimensionsPacket.PACKET_TYPE, SyncRaidDimensionsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncHealthPacket.PACKET_TYPE, SyncHealthPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RaidChallengePacket.PACKET_TYPE, RaidChallengePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RaidChallengePacket.PACKET_TYPE, NetworkMessages::handleRaidChallenge);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SyncRaidDimensionsPacket.PACKET_TYPE, NetworkMessages::handleSyncDim);
        ClientPlayNetworking.registerGlobalReceiver(SyncHealthPacket.PACKET_TYPE, NetworkMessages::handleSyncHealth);
    }

    public static void sendPacketToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    public static void sendPacketToAll(MinecraftServer server, CustomPacketPayload packet) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    public static void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void handleSyncDim(SyncRaidDimensionsPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    public static void handleSyncHealth(SyncHealthPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    public static void handleRaidChallenge(RaidChallengePacket packet, ServerPlayNetworking.Context context) {
        packet.handleServer(context.player());
    }
}
