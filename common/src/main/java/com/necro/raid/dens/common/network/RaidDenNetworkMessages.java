package com.necro.raid.dens.common.network;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.showdown.bagitems.CheerBagItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RaidDenNetworkMessages {
    @FunctionalInterface
    public interface BossChoiceSender {
        void send(ServerPlayer player, UUID raidId, ResourceLocation option1, ResourceLocation option2);
    }

    // S2C Packets
    public static BiConsumer<MinecraftServer, RaidBoss> SYNC_BOSS;
    public static BiConsumer<MinecraftServer, ResourceLocation> REMOVE_SYNCED_BOSS;
    public static Consumer<ServerPlayer> SYNC_REGISTRY;
    public static Consumer<ServerPlayer> SYNC_CONFIG;
    public static BiConsumer<ServerPlayer, Boolean> JOIN_RAID;
    public static BiConsumer<ServerPlayer, String> REQUEST_PACKET;
    public static TriConsumer<ServerPlayer, Float, String> REWARD_PACKET;
    public static BiConsumer<ServerPlayer, Entity> RAID_ASPECT;
    public static TriConsumer<ServerPlayer, String, String> RAID_LOG;
    public static TriConsumer<ServerPlayer, List<Integer>, Boolean> RAID_HEALTH_BAR;
    public static TriConsumer<ServerPlayer, List<Integer>, List<Float>> RAID_HEALTH_UPDATE;
    public static BossChoiceSender CHOOSE_RAID_BOSS;

    // C2S Packets
    public static BiConsumer<PokemonEntity, Pokemon> RAID_CHALLENGE;
    public static Runnable LEAVE_RAID;
    public static BiConsumer<Boolean, String> REQUEST_RESPONSE;
    public static Consumer<Boolean> REWARD_RESPONSE;
    public static Consumer<CheerBagItem.CheerType> USE_CHEER;
    public static BiConsumer<UUID, ResourceLocation> CHOOSE_RAID_BOSS_RESPONSE;
}
