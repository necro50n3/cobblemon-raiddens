package com.necro.raid.dens.common.raids.adventure;

import com.google.gson.JsonSerializer;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import net.minecraft.server.MinecraftServer;

public interface AdventureNode extends JsonSerializer<AdventureNode> {
    void run(RaidAdventureInstance adventure, MinecraftServer server);

    boolean isCompleted();

    default void tick(RaidAdventureInstance adventure, MinecraftServer server) {}


}
