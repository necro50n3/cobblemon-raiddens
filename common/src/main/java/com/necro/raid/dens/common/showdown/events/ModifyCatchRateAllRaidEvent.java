package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record ModifyCatchRateAllRaidEvent(float mod, String operation) implements BroadcastingRaidEvent {
    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {
        if (player == null) return;
        if ("add".equals(this.operation)) raid.addCatchRate(player, this.mod);
        else if ("multiply".equals(this.operation)) raid.mulCatchRate(player, this.mod);
    }
}
