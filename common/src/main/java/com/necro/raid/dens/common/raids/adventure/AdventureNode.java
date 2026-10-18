package com.necro.raid.dens.common.raids.adventure;

import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import net.minecraft.server.MinecraftServer;

public interface AdventureNode {
    void run(RaidAdventureInstance adventure, MinecraftServer server);

    boolean isCompleted();

    default void tick(RaidAdventureInstance adventure, MinecraftServer server) {}
}
