package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface RaidEvent extends AbstractEvent {
    void run(RaidInstance raid, @Nullable ServerPlayer player);
}
