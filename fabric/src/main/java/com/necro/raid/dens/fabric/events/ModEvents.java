package com.necro.raid.dens.fabric.events;

import com.necro.raid.dens.common.client.ClientManager;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackType;

public class ModEvents {
    public static void onPlayerDisconnect(ServerGamePacketListenerImpl serverGamePacketListenerImpl, MinecraftServer minecraftServer) {
        RaidHelper.onPlayerDisconnect(serverGamePacketListenerImpl.getPlayer());
    }

    public static void initRaidHelper(MinecraftServer server) {
        RaidHelper.initHelper(server);
    }

    public static void onServerClose(MinecraftServer server) {
        RaidHelper.onServerClose();
    }

    public static void serverTick(MinecraftServer server) {
        RaidHelper.serverTick();
    }

    public static void commonTick(MinecraftServer server) {
        RaidHelper.commonTick();
    }

    public static void clientTick(Minecraft client) {
        ClientManager.clientTick();
    }

    public static void initRaidBosses(MinecraftServer server) {
        RaidRegistry.initRaidBosses(server);
    }
}
