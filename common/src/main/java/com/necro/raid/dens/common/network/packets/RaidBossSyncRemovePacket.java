package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientRaidRegistry;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RaidBossSyncRemovePacket(ResourceLocation boss) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_boss_sync_remove");
    public static final Type<RaidBossSyncRemovePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidBossSyncRemovePacket> CODEC = StreamCodec.ofMember(RaidBossSyncRemovePacket::write, RaidBossSyncRemovePacket::read);

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(this.boss);
    }

    public static RaidBossSyncRemovePacket read(RegistryFriendlyByteBuf buf) {
        return new RaidBossSyncRemovePacket(buf.readResourceLocation());
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        ClientRaidRegistry.remove(this.boss);
    }
}
