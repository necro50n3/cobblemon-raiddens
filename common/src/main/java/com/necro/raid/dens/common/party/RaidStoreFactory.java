package com.necro.raid.dens.common.party;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.api.storage.PokemonStore;
import com.cobblemon.mod.common.api.storage.StorePosition;
import com.cobblemon.mod.common.api.storage.factory.PokemonStoreFactory;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyReferencePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RaidStoreFactory implements PokemonStoreFactory {
    private static final Map<UUID, RaidPartyStore> PARTY_STORES = new HashMap<>();

    public static boolean hasPlayerParty(UUID uuid) {
        return PARTY_STORES.containsKey(uuid);
    }

    public static void addPlayerParty(ServerPlayer player, List<Pokemon> party) {
        CobblemonRaidDens.LOGGER.info("Adding!");
        RaidPartyStore partyStore = RaidPartyStore.fromParty(player.getUUID(), party);
        PARTY_STORES.put(player.getUUID(), partyStore);
        partyStore.initialize();
        partyStore.sendTo(player);
    }

    public static void removePlayerParty(ServerPlayer player) {
        CobblemonRaidDens.LOGGER.info("Removing!");
        RaidPartyStore removed = PARTY_STORES.remove(player.getUUID());
        if (removed != null) {
            PlayerPartyStore realParty = PlayerExtensionsKt.party(player);
            realParty.sendTo(player);
            CobblemonNetwork.sendPacketToPlayers(List.of(player), new SetPartyReferencePacket(realParty.getUuid()));
        }
    }

    @Override
    public @Nullable PlayerPartyStore getPlayerParty(@NotNull UUID uuid, @NotNull RegistryAccess registryAccess) {
        return PARTY_STORES.get(uuid);
    }

    @Override
    public @Nullable PCStore getPC(@NotNull UUID uuid, @NotNull RegistryAccess registryAccess) {
        return null;
    }

    @Override
    public @Nullable <E extends StorePosition, T extends PokemonStore<E>> T getCustomStore(@NotNull Class<T> aClass, @NotNull UUID uuid, @NotNull RegistryAccess registryAccess) {
        PlayerPartyStore store = PARTY_STORES.get(uuid);
        return aClass.isInstance(store) ? aClass.cast(store) : null;
    }

    @Override
    public void shutdown(@NotNull RegistryAccess registryAccess) {
        PARTY_STORES.clear();
    }

    @Override
    public void onPlayerDisconnect(@NotNull ServerPlayer serverPlayer) {}
}
