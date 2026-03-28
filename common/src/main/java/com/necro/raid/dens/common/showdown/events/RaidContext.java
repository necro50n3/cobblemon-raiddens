package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RaidContext {
    private final RaidInstance raid;
    private final @Nullable ServerPlayer sourcePlayer;
    private final List<ServerPlayer> allPlayers;
    private final @Nullable PokemonBattle sourceBattle;
    private final List<PokemonBattle> allBattles;

    public RaidContext(RaidInstance raid, @Nullable PokemonBattle battle) {
        this.raid = raid;
        this.allPlayers = raid.getPlayers();
        this.allBattles = raid.getBattles();

        this.sourcePlayer = battle == null || battle.getPlayers().isEmpty() ? null : battle.getPlayers().getFirst();
        if (battle != null) this.sourceBattle = battle;
        else if (this.allBattles.isEmpty()) this.sourceBattle = null;
        else this.sourceBattle = this.allBattles.getFirst();
    }

    public RaidInstance raid() {
        return this.raid;
    }

    public @Nullable ServerPlayer player() {
        return this.sourcePlayer;
    }

    public List<ServerPlayer> players() {
        return this.allPlayers;
    }

    public @Nullable PokemonBattle battle() {
        return this.sourceBattle;
    }

    public List<PokemonBattle> battles() {
        return this.allBattles;
    }
}
