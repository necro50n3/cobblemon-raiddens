package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RewardPokemonEvent extends Cancelable {
    private final ServerPlayer player;
    private final Pokemon pokemon;

    public RewardPokemonEvent(@NotNull ServerPlayer player, @NotNull Pokemon pokemon) {
        this.player = player;
        this.pokemon = pokemon;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public Pokemon getPokemon() {
        return this.pokemon;
    }
}
