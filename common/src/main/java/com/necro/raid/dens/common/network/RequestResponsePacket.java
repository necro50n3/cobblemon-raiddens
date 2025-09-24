package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidJoinEvent;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RequestHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public record RequestResponsePacket(boolean accept, String player) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "request_response");
    public static final Type<RequestResponsePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RequestResponsePacket> CODEC = StreamCodec.ofMember(RequestResponsePacket::write, RequestResponsePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.accept);
        buf.writeUtf(this.player);
    }

    public static RequestResponsePacket read(FriendlyByteBuf buf) {
        return new RequestResponsePacket(buf.readBoolean(), buf.readUtf());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleServer(ServerPlayer host) {
        RequestHandler handler = RaidHelper.getRequest(host);
        if (handler == null) return;
        Player player = handler.getPlayer(this.player);
        if (player == null) return;
        RaidCrystalBlockEntity blockEntity = handler.getBlockEntity();
        if (this.accept) this.acceptRequest(host, player, blockEntity);
        else this.denyRequest(host, player);
    }

    private void acceptRequest(ServerPlayer host, Player player, RaidCrystalBlockEntity blockEntity) {
        if (blockEntity.isFull()) {
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.lobby_is_full"));
            return;
        }

        boolean success = RaidEvents.RAID_JOIN.postWithResult(new RaidJoinEvent((ServerPlayer) player, false, blockEntity.getRaidBoss()));
        if (!success) return;

        if (RaidHelper.JOIN_QUEUE.containsKey(player) && !RaidHelper.isAlreadyParticipating(player)) {
            RaidHelper.JOIN_QUEUE.remove(player);
            RaidHelper.addParticipant(player);
            blockEntity.addPlayer(player);
            player.teleportTo(blockEntity.getDimension(), 0.5, 0, -0.5, new HashSet<>(), 180f, 0f);
            player.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.accepted_request"));
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.confirm_accept_request"));
        }
        else {
            RaidHelper.JOIN_QUEUE.remove(player);
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
        }
    }

    private void denyRequest(ServerPlayer host, Player player) {
        if (RaidHelper.JOIN_QUEUE.containsKey(player)) {
            RaidHelper.JOIN_QUEUE.get(player).refundItem();
            RaidHelper.JOIN_QUEUE.remove(player);
            player.sendSystemMessage(RaidHelper.getSystemMessage(
                Component.translatable("message.cobblemonraiddens.raid.rejected_request", host.getName()))
            );
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.confirm_deny_request"));
        }
        else {
            host.sendSystemMessage(RaidHelper.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
        }
    }
}
