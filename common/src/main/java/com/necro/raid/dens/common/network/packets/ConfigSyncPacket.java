package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.config.RaidConfigSync;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ConfigSyncPacket implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "config_sync");
    public static final Type<ConfigSyncPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPacket> CODEC = StreamCodec.ofMember(ConfigSyncPacket::write, ConfigSyncPacket::read);

    private int requiredEnergy;

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(CobblemonRaidDens.CONFIG.required_energy);
    }

    public static ConfigSyncPacket read(FriendlyByteBuf buf) {
        ConfigSyncPacket packet = new ConfigSyncPacket();
        packet.requiredEnergy = buf.readInt();
        return packet;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        RaidConfigSync.required_energy = this.requiredEnergy;
    }
}
