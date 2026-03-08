package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.ClientPacket;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record RaidHealthBarPacket(List<Integer> entityIds, boolean shouldRender) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_health_bar");
    public static final Type<RaidHealthBarPacket> PACKET_TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, RaidHealthBarPacket> CODEC = StreamCodec.ofMember(RaidHealthBarPacket::write, RaidHealthBarPacket::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.entityIds, FriendlyByteBuf::writeInt);
        buf.writeBoolean(this.shouldRender);
    }

    public static RaidHealthBarPacket read(FriendlyByteBuf buf) {
        return new RaidHealthBarPacket(buf.readCollection(ArrayList::new, FriendlyByteBuf::readInt), buf.readBoolean());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    @Override
    public void handleClient() {
        if (Minecraft.getInstance().level == null) return;
        for (int entityId : this.entityIds()) {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity instanceof PokemonEntity pokemon) ((IRaidAccessor) pokemon).crd_setShouldRenderHpBar(this.shouldRender());
        }
    }
}
