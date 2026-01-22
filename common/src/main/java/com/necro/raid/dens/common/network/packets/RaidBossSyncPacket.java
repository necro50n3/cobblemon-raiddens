package com.necro.raid.dens.common.network.packets;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.network.ClientPacket;
import com.necro.raid.dens.common.registry.RaidRegistry;
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
        if (boss.getDisplaySpecies() == null) boss.createDisplayAspects();

        buf.writeResourceLocation(boss.getId());
        buf.writeEnum(boss.getTier());
        buf.writeEnum(boss.getType());
        buf.writeEnum(boss.getFeature());
        buf.writeFloat(boss.getShinyRate());
        buf.writeInt(boss.getMaxCatches());


        buf.writeResourceLocation(boss.getDisplaySpecies().getResourceIdentifier());
        buf.writeInt(boss.getDisplayAspects().size());
        boss.getDisplayAspects().forEach(buf::writeUtf);
    }

    private static RaidBoss readEntry(FriendlyByteBuf buf) {
        RaidBoss boss = new RaidBoss();
        boss.setId(buf.readResourceLocation());
        boss.setTier(buf.readEnum(RaidTier.class));
        boss.setType(buf.readEnum(RaidType.class));
        boss.setFeature(buf.readEnum(RaidFeature.class));
        boss.setShinyRate(buf.readFloat());
        boss.setMaxCatches(buf.readInt());

        boss.setDisplaySpecies(PokemonSpecies.getByIdentifier(buf.readResourceLocation()));
        int count = buf.readInt();
        Set<String> aspects = new HashSet<>();
        for (int i = 0; i < count; i++)  aspects.add(buf.readUtf());
        boss.setDisplayAspects(aspects);

        return boss;
    }

    @Override
    public void handleClient() {
        this.registry.forEach(RaidRegistry::register);
    }
}
