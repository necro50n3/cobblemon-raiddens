package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record AddScriptRaidEvent(RaidTrigger<?> trigger) implements RaidEvent {
    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {
        raid.addTrigger(this.trigger);
    }
}
