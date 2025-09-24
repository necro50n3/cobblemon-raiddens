package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.screens.RaidRequestOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RequestPacket(String player) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_request");
    public static final Type<RequestPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RequestPacket> CODEC = StreamCodec.ofMember(RequestPacket::write, RequestPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.player);
    }

    public static RequestPacket read(FriendlyByteBuf buf) {
        return new RequestPacket(buf.readUtf());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        RaidDenGuiManager.OVERLAY_QUEUE.add(new RaidRequestOverlay(this.player));
    }
}
