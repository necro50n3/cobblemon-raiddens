package com.necro.raid.dens.common.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;

public class RaidDenNetworkMessages {
    public static BiConsumer<ServerPlayer, Float> SYNC_HEALTH;
    public static BiConsumer<ServerPlayer, String> REQUEST_PACKET;
    public static TriConsumer<ServerPlayer, Boolean, String> REWARD_PACKET;
    public static TriConsumer<ServerLevel, Entity, Float> RESIZE;
}
