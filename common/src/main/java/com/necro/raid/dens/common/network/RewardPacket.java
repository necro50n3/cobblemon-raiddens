package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.gui.screens.RaidRewardOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RewardPacket(boolean isCatchable, String pokemon) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_reward");
    public static final Type<RewardPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RewardPacket> CODEC = StreamCodec.ofMember(RewardPacket::write, RewardPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isCatchable);
        buf.writeUtf(this.pokemon);
    }

    public static RewardPacket read(FriendlyByteBuf buf) {
        return new RewardPacket(buf.readBoolean(), buf.readUtf());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        RaidDenGuiManager.OVERLAY_QUEUE.add(new RaidRewardOverlay(this.isCatchable, this.pokemon));
    }
}
