package com.necro.raid.dens.common.network.packets;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientRaidRegistry;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.network.ClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record RaidBossSyncPacket(Collection<RaidBoss> registry) implements CustomPacketPayload, ClientPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_boss_sync");
    public static final CustomPacketPayload.Type<RaidBossSyncPacket> PACKET_TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, RaidBossSyncPacket> CODEC = StreamCodec.ofMember(RaidBossSyncPacket::write, RaidBossSyncPacket::read);

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(this.registry, RaidBossSyncPacket::writeEntry);
    }

    public static RaidBossSyncPacket read(RegistryFriendlyByteBuf buf) {
        return new RaidBossSyncPacket(buf.readCollection(ArrayList::new, RaidBossSyncPacket::readEntry));
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }

    private static void writeEntry(FriendlyByteBuf buf, RaidBoss boss) {
        if (boss.getDisplaySpeciesIdentifier() == null) boss.createDisplayAspects();

        buf.writeResourceLocation(boss.getId());
        buf.writeEnum(boss.getTier());
        buf.writeEnum(boss.getType());
        buf.writeEnum(boss.getFeature());
        buf.writeFloat(boss.getShinyRate());
        buf.writeInt(boss.getMaxCatches());

        buf.writeResourceLocation(boss.getDisplaySpeciesIdentifier());
        buf.writeCollection(boss.getDisplayAspects(), FriendlyByteBuf::writeUtf);
    }

    private static RaidBoss readEntry(FriendlyByteBuf buf) {
        RaidBoss boss = new RaidBoss();
        boss.setId(buf.readResourceLocation());
        boss.setTier(buf.readEnum(RaidTier.class));
        boss.setType(buf.readEnum(RaidType.class));
        boss.setFeature(buf.readEnum(RaidFeature.class));
        boss.setShinyRate(buf.readFloat());
        boss.setMaxCatches(buf.readInt());

        boss.setDisplaySpecies(buf.readResourceLocation());
        boss.setDisplayAspects(buf.readCollection(HashSet::new, FriendlyByteBuf::readUtf));

        return boss;
    }

    @Override
    public void handleClient() {
        ClientRaidRegistry.clear();
        for (RaidBoss boss : this.registry) ClientRaidRegistry.register(boss);
    }
}
