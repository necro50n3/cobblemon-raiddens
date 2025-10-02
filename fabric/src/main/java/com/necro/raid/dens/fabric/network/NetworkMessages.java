package com.necro.raid.dens.fabric.network;

import com.necro.raid.dens.common.network.packets.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class NetworkMessages {
    public static void registerPayload() {
        PayloadTypeRegistry.playS2C().register(SyncRaidDimensionsPacket.PACKET_TYPE, SyncRaidDimensionsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncHealthPacket.PACKET_TYPE, SyncHealthPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(JoinRaidPacket.PACKET_TYPE, JoinRaidPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RequestPacket.PACKET_TYPE, RequestPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RewardPacket.PACKET_TYPE, RewardPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ResizePacket.PACKET_TYPE, ResizePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RaidAspectPacket.PACKET_TYPE, RaidAspectPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(RaidChallengePacket.PACKET_TYPE, RaidChallengePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(LeaveRaidPacket.PACKET_TYPE, LeaveRaidPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestResponsePacket.PACKET_TYPE, RequestResponsePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RewardResponsePacket.PACKET_TYPE, RewardResponsePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RaidChallengePacket.PACKET_TYPE, NetworkMessages::handleRaidChallenge);
        ServerPlayNetworking.registerGlobalReceiver(LeaveRaidPacket.PACKET_TYPE, NetworkMessages::handleLeaveRaid);
        ServerPlayNetworking.registerGlobalReceiver(RequestResponsePacket.PACKET_TYPE, NetworkMessages::handleRequestResponse);
        ServerPlayNetworking.registerGlobalReceiver(RewardResponsePacket.PACKET_TYPE, NetworkMessages::handleRewardResponse);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SyncRaidDimensionsPacket.PACKET_TYPE, NetworkMessages::handleSyncDim);
        ClientPlayNetworking.registerGlobalReceiver(SyncHealthPacket.PACKET_TYPE, NetworkMessages::handleSyncHealth);
        ClientPlayNetworking.registerGlobalReceiver(JoinRaidPacket.PACKET_TYPE, NetworkMessages::handleJoinRaid);
        ClientPlayNetworking.registerGlobalReceiver(RequestPacket.PACKET_TYPE, NetworkMessages::handleRequest);
        ClientPlayNetworking.registerGlobalReceiver(RewardPacket.PACKET_TYPE, NetworkMessages::handleReward);
        ClientPlayNetworking.registerGlobalReceiver(ResizePacket.PACKET_TYPE, NetworkMessages::handleResize);
        ClientPlayNetworking.registerGlobalReceiver(RaidAspectPacket.PACKET_TYPE, NetworkMessages::handleRaidAspect);
    }

    public static void sendPacketToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    public static void sendPacketToAll(MinecraftServer server, CustomPacketPayload packet) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    public static void sendPacketToLevel(ServerLevel level, CustomPacketPayload packet) {
        level.players().forEach(player -> ServerPlayNetworking.send(player, packet));
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

    public static void handleJoinRaid(JoinRaidPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    public static void handleLeaveRaid(LeaveRaidPacket packet, ServerPlayNetworking.Context context) {
        packet.handleServer(context.player());
    }

    public static void handleRequest(RequestPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    public static void handleRequestResponse(RequestResponsePacket packet, ServerPlayNetworking.Context context) {
        packet.handleServer(context.player());
    }

    public static void handleReward(RewardPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    public static void handleRewardResponse(RewardResponsePacket packet, ServerPlayNetworking.Context context) {
        packet.handleServer(context.player());
    }

    public static void handleResize(ResizePacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    public static void handleRaidAspect(RaidAspectPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }
}
