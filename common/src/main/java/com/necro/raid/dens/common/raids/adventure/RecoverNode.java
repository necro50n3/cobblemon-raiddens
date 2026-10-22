package com.necro.raid.dens.common.raids.adventure;

import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.necro.raid.dens.common.raids.RaidAdventureInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Type;

public class RecoverNode implements AdventureNode {
    public static final String ID = "recover";

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

    @Override
    public void onPlayerJoin(ServerPlayer player, RaidAdventureInstance adventure) {
        PlayerExtensionsKt.party(player).heal();
    }

    @Override
    public JsonElement serialize(AdventureNode src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", ID);
        return object;
    }
}
