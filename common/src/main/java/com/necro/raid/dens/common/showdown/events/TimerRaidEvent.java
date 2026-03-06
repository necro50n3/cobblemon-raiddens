package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record TimerRaidEvent(int time) implements RaidEvent {
    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {
        raid.initTimer(this.time);
    }
}
