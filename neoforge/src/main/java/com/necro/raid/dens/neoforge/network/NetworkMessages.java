package com.necro.raid.dens.neoforge.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.packets.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = CobblemonRaidDens.MOD_ID)
public class NetworkMessages {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(CobblemonRaidDens.MOD_ID).versioned("1.0.0").optional();

        payloadRegistrar.playToClient(SyncRaidDimensionsPacket.PACKET_TYPE, SyncRaidDimensionsPacket.CODEC, NetworkMessages::handleSyncDim);
        payloadRegistrar.playToClient(SyncHealthPacket.PACKET_TYPE, SyncHealthPacket.CODEC, NetworkMessages::handleSyncHealth);
        payloadRegistrar.playToClient(JoinRaidPacket.PACKET_TYPE, JoinRaidPacket.CODEC, NetworkMessages::handleJoinRaid);
        payloadRegistrar.playToClient(RequestPacket.PACKET_TYPE, RequestPacket.CODEC, NetworkMessages::handleRequest);
        payloadRegistrar.playToClient(RewardPacket.PACKET_TYPE, RewardPacket.CODEC, NetworkMessages::handleReward);
        payloadRegistrar.playToClient(RewardPacket.PACKET_TYPE, RewardPacket.CODEC, NetworkMessages::handleReward);
        payloadRegistrar.playToClient(ResizePacket.PACKET_TYPE, ResizePacket.CODEC, NetworkMessages::handleResize);
        payloadRegistrar.playToClient(RaidAspectPacket.PACKET_TYPE, RaidAspectPacket.CODEC, NetworkMessages::handleRaidAspect);

        payloadRegistrar.playToServer(RaidChallengePacket.PACKET_TYPE, RaidChallengePacket.CODEC, NetworkMessages::handleRaidChallenge);
        payloadRegistrar.playToServer(LeaveRaidPacket.PACKET_TYPE, LeaveRaidPacket.CODEC, NetworkMessages::handleLeaveRaid);
        payloadRegistrar.playToServer(RequestResponsePacket.PACKET_TYPE, RequestResponsePacket.CODEC, NetworkMessages::handleRequestResponse);
        payloadRegistrar.playToServer(RewardResponsePacket.PACKET_TYPE, RewardResponsePacket.CODEC, NetworkMessages::handleRewardResponse);
    }

    public static void sendPacketToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendPacketToAll(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    public static void sendPacketToLevel(ServerLevel level, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayersInDimension(level, packet);
    }

    public static void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void handleSyncDim(SyncRaidDimensionsPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    public static void handleSyncHealth(SyncHealthPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    public static void handleRaidChallenge(RaidChallengePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handleServer((ServerPlayer) context.player()));
    }

    public static void handleJoinRaid(JoinRaidPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    public static void handleLeaveRaid(LeaveRaidPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handleServer((ServerPlayer) context.player()));
    }

    public static void handleRequest(RequestPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    public static void handleRequestResponse(RequestResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handleServer((ServerPlayer) context.player()));
    }

    public static void handleReward(RewardPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    public static void handleRewardResponse(RewardResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handleServer((ServerPlayer) context.player()));
    }

    public static void handleResize(ResizePacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    public static void handleRaidAspect(RaidAspectPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }
}
