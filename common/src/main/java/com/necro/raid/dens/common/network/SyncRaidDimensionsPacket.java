package com.necro.raid.dens.common.network;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record SyncRaidDimensionsPacket(ResourceKey<Level> levelKey, boolean create) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "sync_dimensions");
    public static final Type<SyncRaidDimensionsPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SyncRaidDimensionsPacket> CODEC = StreamCodec.ofMember(SyncRaidDimensionsPacket::write, SyncRaidDimensionsPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceKey(this.levelKey);
        buf.writeBoolean(create);
    }

    public static SyncRaidDimensionsPacket read(FriendlyByteBuf buf) {
        return new SyncRaidDimensionsPacket(buf.readResourceKey(Registries.DIMENSION), buf.readBoolean());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Set<ResourceKey<Level>> levels = player.connection.levels();
        if (this.create()) levels.add(this.levelKey());
        else levels.remove(this.levelKey());
    }
}