package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidBoss;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RaidBattleStartEvent {
    private final ServerPlayer player;
    private final RaidBoss raidBoss;
    private final PokemonBattle battle;

    public RaidBattleStartEvent(@NotNull ServerPlayer player, @NotNull RaidBoss raidBoss, @NotNull PokemonBattle battle) {
        this.player = player;
        this.raidBoss = raidBoss;
        this.battle = battle;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public PokemonBattle getBattle() {
        return this.battle;
    }
}
