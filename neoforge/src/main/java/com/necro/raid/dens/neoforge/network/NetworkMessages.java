package com.necro.raid.dens.neoforge.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.RaidChallengePacket;
import com.necro.raid.dens.common.network.SyncHealthPacket;
import com.necro.raid.dens.common.network.SyncRaidDimensionsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
        payloadRegistrar.playToServer(RaidChallengePacket.PACKET_TYPE, RaidChallengePacket.CODEC, NetworkMessages::handleRaidChallenge);
    }

    public static void sendPacketToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendPacketToAll(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
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
}
