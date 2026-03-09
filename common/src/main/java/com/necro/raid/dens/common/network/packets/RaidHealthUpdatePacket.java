package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record RaidHealthUpdatePacket(List<Integer> entityIds, List<Float> health) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_health_update");
    public static final Type<RaidHealthUpdatePacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RaidHealthUpdatePacket> CODEC = StreamCodec.ofMember(RaidHealthUpdatePacket::write, RaidHealthUpdatePacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.entityIds, FriendlyByteBuf::writeInt);
        buf.writeCollection(this.health, FriendlyByteBuf::writeFloat);
    }

    public static RaidHealthUpdatePacket read(FriendlyByteBuf buf) {
        return new RaidHealthUpdatePacket(buf.readCollection(ArrayList::new, FriendlyByteBuf::readInt), buf.readCollection(ArrayList::new, FriendlyByteBuf::readFloat));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        if (Minecraft.getInstance().level == null) return;
        for (int i = 0; i < this.entityIds().size(); i++) {
            int entityId = this.entityIds().get(i);
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity instanceof PokemonEntity pokemon) pokemon.getPokemon().setCurrentHealth((int) (this.health().get(i) * pokemon.getPokemon().getMaxHealth()));
        }
    }
}
