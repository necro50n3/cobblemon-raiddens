package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.network.ServerPacket;
import com.necro.raid.dens.common.raids.RaidSession;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChooseRaidBossResponsePacket(UUID raidId, ResourceLocation selectedBoss) implements CustomPacketPayload, ServerPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "choose_raid_boss_response");
    public static final Type<ChooseRaidBossResponsePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ChooseRaidBossResponsePacket> CODEC = StreamCodec.ofMember(ChooseRaidBossResponsePacket::write, ChooseRaidBossResponsePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.raidId);
        buf.writeResourceLocation(this.selectedBoss);
    }

    public static ChooseRaidBossResponsePacket read(FriendlyByteBuf buf) {
        return new ChooseRaidBossResponsePacket(buf.readUUID(), buf.readResourceLocation());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleServer(ServerPlayer player) {
        RaidSession session = RaidHelper.ACTIVE_RAIDS.get(this.raidId);
        RaidDenNetworkMessages.REMOVE_BOSS_SELECTOR.accept(player, this.raidId);
        session.onBossSelected(player, this.selectedBoss);
    }
}
