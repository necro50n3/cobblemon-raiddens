package com.necro.raid.dens.common.raids.adventure;

import com.google.gson.JsonSerializer;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface AdventureNode extends JsonSerializer<AdventureNode> {
    default int delay() {
        return 0;
    }

    void run(RaidAdventureInstance adventure, MinecraftServer server);

    boolean isCompleted();

    default void tick(RaidAdventureInstance adventure, MinecraftServer server) {}

    default void onPlayerJoin(ServerPlayer player, RaidAdventureInstance adventure) {}

    default void onPlayerLeave(ServerPlayer player, RaidAdventureInstance adventure) {}
}
