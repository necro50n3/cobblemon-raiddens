package com.necro.raid.dens.common.raids.adventure;

import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class RecoverNode implements AdventureNode {
    private transient boolean completed = false;

    @Override
    public void run(RaidAdventureInstance adventure, MinecraftServer server) {
        for (ServerPlayer player : adventure.getPlayers()) {
            if (player != null) PlayerExtensionsKt.party(player).heal();
        }
        this.completed = true;
    }

    @Override
    public boolean isCompleted() {
        return this.completed;
    }
}
