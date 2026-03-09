package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record CatchRateMultiplyRaidEvent(float mod) implements RaidEvent {
    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {
        if (player == null) return;
        raid.mulCatchRate(player, this.mod);
    }
}
