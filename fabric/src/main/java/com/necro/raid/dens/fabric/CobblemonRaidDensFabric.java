package com.necro.raid.dens.fabric;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.commands.RaidAdminCommands;
import com.necro.raid.dens.common.commands.RaidDenCommands;
import com.necro.raid.dens.common.commands.RaidRequestCommands;
import com.necro.raid.dens.common.commands.RaidRewardCommands;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.network.SyncRaidDimensionsPacket;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.util.RaidRegistry;
import com.necro.raid.dens.fabric.advancements.FabricCriteriaTriggers;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import com.necro.raid.dens.fabric.compat.distanthorizons.FabricDistantHorizonsCompat;
import com.necro.raid.dens.fabric.compat.megashowdown.FabricMSDCompat;
import com.necro.raid.dens.fabric.components.FabricComponents;
import com.necro.raid.dens.fabric.events.RaidBossResourceReloadListener;
import com.necro.raid.dens.fabric.items.FabricItems;
import com.necro.raid.dens.fabric.items.FabricPredicates;
import com.necro.raid.dens.fabric.items.RaidDenTab;
import com.necro.raid.dens.common.network.SyncHealthPacket;
import com.necro.raid.dens.common.raids.RaidBuilder;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.fabric.dimensions.FabricDimensions;
import com.necro.raid.dens.fabric.events.ModEvents;
import com.necro.raid.dens.fabric.loot.FabricLootFunctions;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import com.necro.raid.dens.fabric.statistics.FabricStatistics;
import com.necro.raid.dens.fabric.worldgen.FabricFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
        ServerTickEvents.END_SERVER_TICK.register(DimensionHelper::removePending);
        CommandRegistrationCallback.EVENT.register(RaidAdminCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidRequestCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidRewardCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidDenCommands::register);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(DimensionHelper::onDimensionChange);

        DynamicRegistries.register(RaidRegistry.RAID_BOSS_KEY, RaidBoss.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBossResourceReloadListener());

        RaidBuilder.SYNC_HEALTH = (player, healthRatio) ->
            NetworkMessages.sendPacketToPlayer(player, new SyncHealthPacket(healthRatio));
        DimensionHelper.SYNC_DIMENSIONS = (server, levelKey, create) ->
            NetworkMessages.sendPacketToAll(server, new SyncRaidDimensionsPacket(levelKey, create));
    }

}
