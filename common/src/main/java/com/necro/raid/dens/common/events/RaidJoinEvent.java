package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import com.necro.raid.dens.common.raids.RaidBoss;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RaidJoinEvent extends Cancelable {
    private final ServerPlayer player;
    private final boolean isHost;
    private final RaidBoss raidBoss;

    public RaidJoinEvent(@NotNull ServerPlayer player, boolean isHost, @NotNull RaidBoss raidBoss) {
        this.player = player;
        this.isHost = isHost;
        this.raidBoss = raidBoss;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public boolean isHost() {
        return this.isHost;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }
}
