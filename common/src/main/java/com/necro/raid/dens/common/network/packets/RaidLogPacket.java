package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RaidLogPacket(String pokemon, String move) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_log");
    public static final Type<RaidLogPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RaidLogPacket> CODEC = StreamCodec.ofMember(RaidLogPacket::write, RaidLogPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.pokemon);
        buf.writeUtf(this.move);
    }

    public static RaidLogPacket read(FriendlyByteBuf buf) {
        return new RaidLogPacket(buf.readUtf(), buf.readUtf());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        if (RaidDenGuiManager.RAID_OVERLAY == null) return;

        Component log = Component.translatable(
            "battle.cobblemonraiddens.log.used_move",
            Component.translatable(this.pokemon),
            Component.translatable(this.move)
        );
        RaidDenGuiManager.RAID_OVERLAY.addLog(log);
    }
}
