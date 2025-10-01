package com.necro.raid.dens.fabric;

import com.cobblemon.mod.common.api.Priority;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientRaidBoss;
import com.necro.raid.dens.common.commands.RaidAdminCommands;
import com.necro.raid.dens.common.commands.RaidDenCommands;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.*;
import com.necro.raid.dens.common.network.packets.*;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.structure.RaidDenPool;
import com.necro.raid.dens.common.util.*;
import com.necro.raid.dens.fabric.advancements.FabricCriteriaTriggers;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import com.necro.raid.dens.fabric.compat.distanthorizons.FabricDistantHorizonsCompat;
import com.necro.raid.dens.fabric.compat.megashowdown.FabricMSDCompat;
import com.necro.raid.dens.fabric.components.FabricComponents;
import com.necro.raid.dens.fabric.events.*;
import com.necro.raid.dens.fabric.items.FabricItems;
import com.necro.raid.dens.fabric.items.FabricPredicates;
import com.necro.raid.dens.fabric.items.RaidDenTab;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.fabric.dimensions.FabricDimensions;
import com.necro.raid.dens.fabric.loot.FabricLootFunctions;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import com.necro.raid.dens.fabric.statistics.FabricStatistics;
import com.necro.raid.dens.fabric.worldgen.FabricFeatures;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;

public class CobblemonRaidDensFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobblemonRaidDens.init();

        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(FabricLoader.getInstance().isModLoaded(mod.getModid()));
        }
        if (ModCompat.MEGA_SHOWDOWN.isLoaded()) FabricMSDCompat.init();
        if (ModCompat.DISTANT_HORIZONS.isLoaded()) FabricDistantHorizonsCompat.init();

        NetworkMessages.registerPayload();
        FabricBlocks.registerModBlocks();
        FabricItems.registerItems();
        FabricDimensions.registerChunkGenerator();
        FabricComponents.registerDataComponents();
        FabricPredicates.registerPredicates();
        FabricFeatures.registerFeatures();
        FabricLootFunctions.registerLootFunctions();
        FabricStatistics.registerStatistics();
        FabricCriteriaTriggers.registerCriteriaTriggers();
        RaidDenTab.registerItemGroups();

        ServerLifecycleEvents.SERVER_STARTED.register(ModEvents::initRaidHelper);
        ServerLifecycleEvents.SERVER_STARTED.register(ModEvents::initRaidBosses);
        ServerPlayConnectionEvents.JOIN.register(ModEvents::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(ModEvents::onPlayerDisconnect);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::commonTick);
        CommandRegistrationCallback.EVENT.register(RaidAdminCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidDenCommands::register);
        PlayerBlockBreakEvents.BEFORE.register(RaidUtils::canBreakOrPlace);
        UseBlockCallback.EVENT.register(RaidUtils::canBreakOrPlace);

        DynamicRegistries.registerSynced(RaidRegistry.RAID_BOSS_KEY, RaidBoss.codec(), ClientRaidBoss.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBossResourceReloadListener());

        DynamicRegistries.register(RaidBucketRegistry.BUCKET_KEY, RaidBucket.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBucketResourceReloadListener());

        DynamicRegistries.register(RaidDenRegistry.DEN_KEY, RaidDenPool.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidDenPoolResourceReloadListener());

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidTemplateResourceReloadListener());

        RaidDenNetworkMessages.SYNC_HEALTH = (player, healthRatio) ->
            NetworkMessages.sendPacketToPlayer(player, new SyncHealthPacket(healthRatio));
        RaidDenNetworkMessages.REQUEST_PACKET = (player, name) ->
            NetworkMessages.sendPacketToPlayer(player, new RequestPacket(name));
        RaidDenNetworkMessages.REWARD_PACKET = (player, isCatchable, pokemon) ->
            NetworkMessages.sendPacketToPlayer(player, new RewardPacket(isCatchable, pokemon));
        RaidDenNetworkMessages.RESIZE = (level, entity, scale) ->
            NetworkMessages.sendPacketToLevel(level, new ResizePacket(entity.getId(), scale));

        DimensionHelper.SYNC_DIMENSIONS = (server, levelKey, create) ->
            NetworkMessages.sendPacketToAll(server, new SyncRaidDimensionsPacket(levelKey, create));

        RaidEvents.RAID_JOIN.subscribe(Priority.NORMAL, event -> {
            NetworkMessages.sendPacketToPlayer(event.getPlayer(), new JoinRaidPacket(true));
            return Unit.INSTANCE;
        });
    }
}
