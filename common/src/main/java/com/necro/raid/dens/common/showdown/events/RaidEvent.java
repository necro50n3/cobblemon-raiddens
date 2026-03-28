package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface RaidEvent extends AbstractEvent {
    @Override
    default void execute(RaidContext context) {
        run(context.raid(), context.player());
    }

    void run(RaidInstance raid, @Nullable ServerPlayer player);
}
