package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.events.RaidJoinEvent;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.RequestHandler;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.raids.helpers.RaidRegionHelper;
import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record RequestResponsePacket(boolean accept, String player) implements CustomPacketPayload, ServerPacket {
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

    @Override
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
            host.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.lobby_is_full"));
            return;
        }

        boolean success = RaidEvents.RAID_JOIN.postWithResult(new RaidJoinEvent((ServerPlayer) player, false, blockEntity.getRaidBoss()));
        if (!success) return;

        RaidRegion region = RaidRegionHelper.getRegion(blockEntity.getUuid());

        if (region != null && RaidJoinHelper.isInQueue(player) && !RaidJoinHelper.isParticipating(player, false)) {
            assert player.getServer() != null;
            RaidJoinHelper.removeFromQueue(player, false);
            if (!RaidJoinHelper.addParticipant(player, blockEntity.getUuid(), false, true)) return;

            RaidHelper.ACTIVE_RAIDS.get(blockEntity.getUuid()).addPlayer((ServerPlayer) player);
            RaidUtils.teleportPlayerToRaid((ServerPlayer) player, player.getServer(), region);
            blockEntity.syncAspects((ServerPlayer) player);
        }
        else {
            RaidJoinHelper.removeFromQueue(player, false);
            host.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
        }
    }

    private void denyRequest(ServerPlayer host, Player player) {
        if (RaidJoinHelper.isInQueue(player)) {
            RaidJoinHelper.removeFromQueue(player, true);
            RaidHelper.REQUEST_QUEUE.get(host.getUUID()).removePlayer(player);
            player.sendSystemMessage(ComponentUtils.getSystemMessage(
                Component.translatable("message.cobblemonraiddens.raid.rejected_request", host.getName()))
            );
            host.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.confirm_deny_request"));
        }
        else {
            host.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
        }
    }
}
