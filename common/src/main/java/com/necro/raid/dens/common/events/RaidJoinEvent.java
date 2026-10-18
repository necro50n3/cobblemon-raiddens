package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import com.necro.raid.dens.common.data.RaidSession;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class RaidJoinEvent extends Cancelable {
    private final ServerPlayer player;
    private final boolean isHost;
    private final RaidSession raid;

    public RaidJoinEvent(@NotNull ServerPlayer player, boolean isHost, @NotNull RaidSession raid) {
        this.player = player;
        this.isHost = isHost;
        this.raid = raid;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public boolean isHost() {
        return this.isHost;
    }

    public RaidSession getRaid() {
        return this.raid;
    }
}
