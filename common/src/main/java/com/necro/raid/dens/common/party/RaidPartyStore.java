package com.necro.raid.dens.common.party;

import com.cobblemon.mod.common.advancement.CobblemonCriteria;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket;
import com.cobblemon.mod.common.net.messages.client.storage.party.MoveClientPartyPokemonPacket;
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyPokemonPacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class RaidPartyStore extends PlayerPartyStore {
    public RaidPartyStore(@NotNull UUID playerUUID) {
        super(playerUUID);
    }

    public static RaidPartyStore fromParty(UUID player, List<Pokemon> party) {
        RaidPartyStore raidParty = new RaidPartyStore(player);
        for (int i = 0; i < Math.min(6, party.size()); i++) raidParty.getSlots().set(i, party.get(i));
        return raidParty;
    }

    @Override
    public void initialize() {
        this.rebuildPokemonByUuid();
        this.getObserverUUIDs().add(this.getPlayerUUID());
    }

    @Override
    public void set(@NotNull PartyPosition position, @NotNull Pokemon pokemon) {
        Pokemon existing = this.get(position);
        if (existing == pokemon) return;

        this.setAtPosition(position, pokemon);
        this.rebuildPokemonByUuid();
        this.sendPacketToObservers(new SetPartyPokemonPacket(this.getUuid(), position, access -> pokemon));

        ServerPlayer player = pokemon.getOwnerPlayer();
        if (player != null) CobblemonCriteria.PARTY_CHECK.trigger(player, this);
    }

    @Override
    public void swap(@NotNull PartyPosition position1, @NotNull PartyPosition position2) {
        Pokemon pokemon1 = this.get(position1);
        Pokemon pokemon2 = this.get(position2);

        this.setAtPosition(position1, pokemon2);
        this.setAtPosition(position2, pokemon1);
        this.rebuildPokemonByUuid();

        if (pokemon1 != null && pokemon2 != null) {
            this.sendPacketToObservers(new SwapClientPokemonPacket(this, pokemon1.getUuid(), pokemon2.getUuid()));
            ServerPlayer player = pokemon1.getOwnerPlayer();
            if (player != null) CobblemonCriteria.PARTY_CHECK.trigger(player, this);
        }
        else if (pokemon1 != null || pokemon2 != null) {
            ServerPlayer player = pokemon1 != null ? pokemon1.getOwnerPlayer() : pokemon2.getOwnerPlayer();
            if (player != null) CobblemonCriteria.PARTY_CHECK.trigger(player, this);

            PartyPosition newPosition = pokemon1 == null ? position1 : position2;
            Pokemon pokemon = pokemon1 != null ? pokemon1 : pokemon2;
            this.sendPacketToObservers(new MoveClientPartyPokemonPacket(this.getUuid(), pokemon.getUuid(), newPosition));
        }
    }
}
