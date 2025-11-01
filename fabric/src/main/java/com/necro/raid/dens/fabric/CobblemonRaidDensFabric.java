package com.necro.raid.dens.fabric;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientRaidBoss;
import com.necro.raid.dens.common.commands.RaidAdminCommands;
import com.necro.raid.dens.common.commands.RaidDenCommands;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.network.*;
import com.necro.raid.dens.common.network.packets.*;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidExitHelper;
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
import com.necro.raid.dens.fabric.dimensions.FabricDimensions;
import com.necro.raid.dens.fabric.loot.FabricLootFunctions;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import com.necro.raid.dens.fabric.statistics.FabricStatistics;
import com.necro.raid.dens.fabric.worldgen.FabricFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEvents::onServerStopping);
        ServerPlayConnectionEvents.JOIN.register(ModEvents::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(ModEvents::onPlayerDisconnect);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::commonTick);
        CommandRegistrationCallback.EVENT.register(RaidAdminCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidDenCommands::register);
        PlayerBlockBreakEvents.BEFORE.register(RaidUtils::canBreak);
        UseBlockCallback.EVENT.register(RaidUtils::canPlace);
        ServerPlayerEvents.AFTER_RESPAWN.register(RaidExitHelper::afterRespawn);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(RaidExitHelper::onDimensionChange);

        DynamicRegistries.registerSynced(RaidRegistry.RAID_BOSS_KEY, RaidBoss.codec(), ClientRaidBoss.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBossResourceReloadListener());

        DynamicRegistries.register(RaidBucketRegistry.BUCKET_KEY, RaidBucket.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBucketResourceReloadListener());

        DynamicRegistries.register(RaidDenRegistry.DEN_KEY, RaidDenPool.codec());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidDenPoolResourceReloadListener());

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidTemplateResourceReloadListener());

        NetworkMessages.init();
    }
}
