package com.necro.raid.dens.common.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class RaidDenNetworkMessages {
    public static BiConsumer<ServerPlayer, Float> SYNC_HEALTH;
    public static BiConsumer<ServerPlayer, String> REQUEST_PACKET;
    public static BiConsumer<ServerPlayer, Boolean> REWARD_PACKET;
}
