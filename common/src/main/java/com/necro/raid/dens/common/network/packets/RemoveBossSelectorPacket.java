package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.screens.RaidAdventureOverlay;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RemoveBossSelectorPacket(UUID raidId) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "remove_boss_selector");
    public static final Type<RemoveBossSelectorPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RemoveBossSelectorPacket> CODEC = StreamCodec.ofMember(RemoveBossSelectorPacket::write, RemoveBossSelectorPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.raidId);
    }

    public static RemoveBossSelectorPacket read(FriendlyByteBuf buf) {
        return new RemoveBossSelectorPacket(buf.readUUID());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        RaidDenGuiManager.PRIORITY_QUEUE.removeIf(overlay -> overlay instanceof RaidAdventureOverlay adventure && adventure.raid().equals(this.raidId));
    }
}
