package com.necro.raid.dens.neoforge.network;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.ClientPacket;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.network.packets.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID)
public class NetworkMessages {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar payloadRegistrar = event.registrar(CobblemonRaidDens.MOD_ID).versioned("1.0.0").optional();

        payloadRegistrar.playToClient(RaidBossSyncPacket.PACKET_TYPE, RaidBossSyncPacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToClient(JoinRaidPacket.PACKET_TYPE, JoinRaidPacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToClient(RequestPacket.PACKET_TYPE, RequestPacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToClient(RewardPacket.PACKET_TYPE, RewardPacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToClient(ResizePacket.PACKET_TYPE, ResizePacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToClient(RaidAspectPacket.PACKET_TYPE, RaidAspectPacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToClient(RaidLogPacket.PACKET_TYPE, RaidLogPacket.CODEC, NetworkMessages::handle);

        payloadRegistrar.playToServer(RaidChallengePacket.PACKET_TYPE, RaidChallengePacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToServer(LeaveRaidPacket.PACKET_TYPE, LeaveRaidPacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToServer(RequestResponsePacket.PACKET_TYPE, RequestResponsePacket.CODEC, NetworkMessages::handle);
        payloadRegistrar.playToServer(RewardResponsePacket.PACKET_TYPE, RewardResponsePacket.CODEC, NetworkMessages::handle);
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

    private static void handle(ClientPacket packet, IPayloadContext context) {
        context.enqueueWork(packet::handleClient);
    }

    private static void handle(ServerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> packet.handleServer((ServerPlayer) context.player()));
    }
}
