package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public interface BroadcastingRaidEvent extends RaidEvent {
    @Override
    default void execute(RaidContext context) {
        broadcast(context.raid(), context.players());
    }

    default void broadcast(RaidInstance raid, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            this.run(raid, player);
        }
    }
}
