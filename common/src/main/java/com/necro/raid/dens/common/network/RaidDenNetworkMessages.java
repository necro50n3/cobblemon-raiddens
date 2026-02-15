package com.necro.raid.dens.common.network;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RaidDenNetworkMessages {
    public static BiConsumer<ServerPlayer, Boolean> JOIN_RAID;
    public static BiConsumer<ServerPlayer, String> REQUEST_PACKET;
    public static TriConsumer<ServerPlayer, Boolean, String> REWARD_PACKET;
    public static BiConsumer<ServerPlayer, Entity> RAID_ASPECT;
    public static TriConsumer<ServerPlayer, String, String> RAID_LOG;

    public static BiConsumer<PokemonEntity, Pokemon> RAID_CHALLENGE;
    public static Runnable LEAVE_RAID;
    public static BiConsumer<Boolean, String> REQUEST_RESPONSE;
    public static Consumer<Boolean> REWARD_RESPONSE;
    public static Consumer<ServerPlayer> SYNC_REGISTRY;
}
