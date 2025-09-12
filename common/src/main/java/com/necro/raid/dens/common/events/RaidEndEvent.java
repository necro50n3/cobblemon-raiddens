package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.raids.RaidBoss;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RaidEndEvent {
    private final ServerPlayer player;
    private final RaidBoss raidBoss;
    private final boolean isWin;

    public RaidEndEvent(@NotNull ServerPlayer player, @NotNull RaidBoss raidBoss, boolean isWin) {
        this.player = player;
        this.raidBoss = raidBoss;
        this.isWin = isWin;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }

    public boolean isWin() {
        return this.isWin;
    }
}
