package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record HealRaidEvent(float ratio) implements RaidEvent {
    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {
        raid.healBoss(this.ratio);
    }
}
