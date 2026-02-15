package com.necro.raid.dens.fabric.network;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.necro.raid.dens.common.network.ClientPacket;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.network.packets.*;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class NetworkMessages {
    public static void registerPayload() {
        PayloadTypeRegistry.playS2C().register(RaidBossSyncPacket.PACKET_TYPE, RaidBossSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(JoinRaidPacket.PACKET_TYPE, JoinRaidPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RequestPacket.PACKET_TYPE, RequestPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RewardPacket.PACKET_TYPE, RewardPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ResizePacket.PACKET_TYPE, ResizePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RaidAspectPacket.PACKET_TYPE, RaidAspectPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(RaidLogPacket.PACKET_TYPE, RaidLogPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(RaidChallengePacket.PACKET_TYPE, RaidChallengePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(LeaveRaidPacket.PACKET_TYPE, LeaveRaidPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestResponsePacket.PACKET_TYPE, RequestResponsePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RewardResponsePacket.PACKET_TYPE, RewardResponsePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RaidChallengePacket.PACKET_TYPE, NetworkMessages::handle);
        ServerPlayNetworking.registerGlobalReceiver(LeaveRaidPacket.PACKET_TYPE, NetworkMessages::handle);
        ServerPlayNetworking.registerGlobalReceiver(RequestResponsePacket.PACKET_TYPE, NetworkMessages::handle);
        ServerPlayNetworking.registerGlobalReceiver(RewardResponsePacket.PACKET_TYPE, NetworkMessages::handle);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(RaidBossSyncPacket.PACKET_TYPE, NetworkMessages::handle);
        ClientPlayNetworking.registerGlobalReceiver(JoinRaidPacket.PACKET_TYPE, NetworkMessages::handle);
        ClientPlayNetworking.registerGlobalReceiver(RequestPacket.PACKET_TYPE, NetworkMessages::handle);
        ClientPlayNetworking.registerGlobalReceiver(RewardPacket.PACKET_TYPE, NetworkMessages::handle);
        ClientPlayNetworking.registerGlobalReceiver(ResizePacket.PACKET_TYPE, NetworkMessages::handle);
        ClientPlayNetworking.registerGlobalReceiver(RaidAspectPacket.PACKET_TYPE, NetworkMessages::handle);
        ClientPlayNetworking.registerGlobalReceiver(RaidLogPacket.PACKET_TYPE, NetworkMessages::handle);
    }

    public static void init() {
        RaidDenNetworkMessages.JOIN_RAID = (player, isJoining) ->
            NetworkMessages.sendPacketToPlayer(player, new JoinRaidPacket(isJoining));
        RaidDenNetworkMessages.REQUEST_PACKET = (player, name) ->
            NetworkMessages.sendPacketToPlayer(player, new RequestPacket(name));
        RaidDenNetworkMessages.REWARD_PACKET = (player, isCatchable, pokemon) ->
            NetworkMessages.sendPacketToPlayer(player, new RewardPacket(isCatchable, pokemon));
        RaidDenNetworkMessages.RAID_ASPECT = (player, entity) ->
            NetworkMessages.sendPacketToPlayer(player, new RaidAspectPacket(entity.getId()));
        RaidDenNetworkMessages.RAID_LOG = (player, pokemon, move) ->
            NetworkMessages.sendPacketToPlayer(player, new RaidLogPacket(pokemon, move));

        RaidDenNetworkMessages.RAID_CHALLENGE = (pokemonEntity, pokemon) ->
            NetworkMessages.sendPacketToServer(new RaidChallengePacket(pokemonEntity.getId(), pokemon.getUuid(), BattleFormat.Companion.getGEN_9_SINGLES()));
        RaidDenNetworkMessages.LEAVE_RAID = () -> NetworkMessages.sendPacketToServer(new LeaveRaidPacket());
        RaidDenNetworkMessages.REQUEST_RESPONSE = (accept, player) ->
            NetworkMessages.sendPacketToServer(new RequestResponsePacket(accept, player));
        RaidDenNetworkMessages.REWARD_RESPONSE = (catchPokemon) ->
            NetworkMessages.sendPacketToServer(new RewardResponsePacket(catchPokemon));
        RaidDenNetworkMessages.SYNC_REGISTRY = (player) ->
            NetworkMessages.sendPacketToPlayer(player, new RaidBossSyncPacket(RaidRegistry.RAID_LOOKUP.values()));
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

    private static void handle(ClientPacket packet, ClientPlayNetworking.Context context) {
        packet.handleClient();
    }

    private static void handle(ServerPacket packet, ServerPlayNetworking.Context context) {
        packet.handleServer(context.player());
    }
}
