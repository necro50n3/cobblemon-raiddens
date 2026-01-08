package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.util.IRaidTeleporter;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record LeaveRaidPacket() implements CustomPacketPayload, ServerPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "leave_raid");
    public static final Type<LeaveRaidPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, LeaveRaidPacket> CODEC = StreamCodec.ofMember(LeaveRaidPacket::write, LeaveRaidPacket::read);

    public void write(FriendlyByteBuf buf) {}

    public static LeaveRaidPacket read(FriendlyByteBuf buf) {
        return new LeaveRaidPacket();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RaidUtils.leaveRaid(player);
        if (RaidUtils.isRaidDimension(level)) ((IRaidTeleporter) player).crd_returnHome();
    }
}
