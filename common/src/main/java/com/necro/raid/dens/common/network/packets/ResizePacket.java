package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ResizePacket(int entityId, float scale) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "resize");
    public static final Type<ResizePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ResizePacket> CODEC = StreamCodec.ofMember(ResizePacket::write, ResizePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeFloat(this.scale);
    }

    public static ResizePacket read(FriendlyByteBuf buf) {
        return new ResizePacket(buf.readInt(), buf.readFloat());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    public void handleClient() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity entity = level.getEntity(this.entityId);
        if (!(entity instanceof PokemonEntity pokemonEntity)) return;
        CobblemonRaidDens.LOGGER.info("Resizing ditto");
        pokemonEntity.getPokemon().setScaleModifier(this.scale);
        pokemonEntity.refreshDimensions();
    }
}
