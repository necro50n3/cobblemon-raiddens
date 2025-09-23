package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record RewardResponsePacket(boolean accept) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "reward_response");
    public static final Type<RewardResponsePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RewardResponsePacket> CODEC = StreamCodec.ofMember(RewardResponsePacket::write, RewardResponsePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.accept);
    }

    public static RewardResponsePacket read(FriendlyByteBuf buf) {
        return new RewardResponsePacket(buf.readBoolean());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleServer(ServerPlayer player) {
        CobblemonRaidDens.LOGGER.info("handle reward response");
    }
}
