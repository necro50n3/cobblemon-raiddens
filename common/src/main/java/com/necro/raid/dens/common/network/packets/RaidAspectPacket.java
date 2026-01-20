package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.properties.AspectPropertyType;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record RaidAspectPacket(int entityId) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_aspect");
    public static final Type<RaidAspectPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RaidAspectPacket> CODEC = StreamCodec.ofMember(RaidAspectPacket::write, RaidAspectPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static RaidAspectPacket read(FriendlyByteBuf buf) {
        return new RaidAspectPacket(buf.readInt());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity entity = level.getEntity(this.entityId);
        if (!(entity instanceof PokemonEntity pokemonEntity)) return;
        AspectPropertyType.INSTANCE.fromString("raid").apply(pokemonEntity);
        pokemonEntity.refreshDimensions();
    }
}
