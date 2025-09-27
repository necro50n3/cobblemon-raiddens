package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.screens.RaidOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record JoinRaidPacket(boolean isJoining) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "join_raid");
    public static final Type<JoinRaidPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, JoinRaidPacket> CODEC = StreamCodec.ofMember(JoinRaidPacket::write, JoinRaidPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isJoining);
    }

    public static JoinRaidPacket read(FriendlyByteBuf buf) {
        return new JoinRaidPacket(buf.readBoolean());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        if (this.isJoining) RaidDenGuiManager.RAID_OVERLAY = new RaidOverlay();
        else RaidDenGuiManager.RAID_OVERLAY = null;
    }
}
