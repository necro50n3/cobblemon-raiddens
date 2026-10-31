package com.necro.raid.dens.common.raids.adventure;

import com.google.gson.JsonSerializer;
import com.necro.raid.dens.common.raids.RaidSession;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface AdventureNode extends JsonSerializer<AdventureNode> {
    default int delay() {
        return 0;
    }

    void run(RaidSession adventure, MinecraftServer server);

    boolean isCompleted();

    default void tick(RaidSession adventure, MinecraftServer server) {}

    default void onPlayerJoin(ServerPlayer player, RaidSession adventure) {}

    default void onPlayerLeave(ServerPlayer player, RaidSession adventure) {}

    AdventureNode copy();
}
