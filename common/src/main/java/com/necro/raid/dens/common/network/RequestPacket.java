package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.RaidScreenComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RequestPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_request");
    public static final Type<RequestPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RequestPacket> CODEC = StreamCodec.ofMember(RequestPacket::write, RequestPacket::read);

    public void write(FriendlyByteBuf buf) {}

    public static RequestPacket read(FriendlyByteBuf buf) {
        return new RequestPacket();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        RaidDenGuiManager.OVERLAY_QUEUE.add(RaidScreenComponents.REQUEST_OVERLAY);
    }
}
