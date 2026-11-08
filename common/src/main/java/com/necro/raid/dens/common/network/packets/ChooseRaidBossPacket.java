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

public record ChooseRaidBossPacket(UUID raidId, ResourceLocation option1, ResourceLocation option2) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "choose_raid_boss");
    public static final Type<ChooseRaidBossPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ChooseRaidBossPacket> CODEC = StreamCodec.ofMember(ChooseRaidBossPacket::write, ChooseRaidBossPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.raidId);
        buf.writeResourceLocation(this.option1);
        buf.writeResourceLocation(this.option2);
    }

    public static ChooseRaidBossPacket read(FriendlyByteBuf buf) {
        return new ChooseRaidBossPacket(buf.readUUID(), buf.readResourceLocation(), buf.readResourceLocation());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        RaidDenGuiManager.PRIORITY_QUEUE.add(new RaidAdventureOverlay(this.raidId, this.option1, this.option2));
    }
}
