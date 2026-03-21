package com.necro.raid.dens.common.showdown.events;

import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface BroadcastingRaidEvent extends RaidEvent {
    default void broadcast(RaidInstance raid, List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            this.run(raid, player);
        }
    }
}
