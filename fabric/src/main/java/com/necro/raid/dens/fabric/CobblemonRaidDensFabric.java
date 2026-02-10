package com.necro.raid.dens.fabric;

import com.cobblemon.mod.common.Cobblemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.commands.RaidAdminCommands;
import com.necro.raid.dens.common.commands.RaidDenCommands;
import com.necro.raid.dens.common.commands.RaidSpawnCommands;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.network.*;
import com.necro.raid.dens.common.network.packets.*;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.common.showdown.RaidDensShowdownRegistry;
import com.necro.raid.dens.common.util.*;
import com.necro.raid.dens.fabric.advancements.FabricCriteriaTriggers;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import com.necro.raid.dens.fabric.components.FabricComponents;
import com.necro.raid.dens.fabric.events.*;
import com.necro.raid.dens.fabric.events.reloader.*;
import com.necro.raid.dens.fabric.items.FabricItems;
import com.necro.raid.dens.fabric.items.FabricPredicates;
import com.necro.raid.dens.fabric.items.RaidDenTab;
import com.necro.raid.dens.fabric.dimensions.FabricDimensions;
import com.necro.raid.dens.fabric.loot.FabricLootConditions;
import com.necro.raid.dens.fabric.loot.FabricLootFunctions;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import com.necro.raid.dens.fabric.statistics.FabricStatistics;
import com.necro.raid.dens.fabric.worldgen.FabricFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.server.packs.PackType;

public class CobblemonRaidDensFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobblemonRaidDens.init();

        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(FabricLoader.getInstance().isModLoaded(mod.getModid()));
        }

        if (!isCobblemon171()) RaidDensShowdownRegistry.registerInstructions();

        NetworkMessages.registerPayload();
        FabricBlocks.registerModBlocks();
        FabricComponents.registerDataComponents();
        FabricItems.registerItems();
        FabricDimensions.registerChunkGenerator();
        FabricPredicates.registerPredicates();
        FabricFeatures.registerFeatures();
        FabricLootConditions.registerLootConditions();
        FabricLootFunctions.registerLootFunctions();
        FabricStatistics.registerStatistics();
        FabricCriteriaTriggers.registerCriteriaTriggers();
        RaidDenTab.registerItemGroups();

        ServerLifecycleEvents.SERVER_STARTED.register(ModEvents::initRaidHelper);
        ServerLifecycleEvents.SERVER_STARTED.register(ModEvents::initRaidBosses);
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEvents::onServerClose);
        ServerPlayConnectionEvents.JOIN.register(ModEvents::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(ModEvents::onPlayerDisconnect);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::commonTick);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::serverTick);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(ModDimensions::onDimensionChange);
        CommandRegistrationCallback.EVENT.register(RaidAdminCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidDenCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidSpawnCommands::register);
        PlayerBlockBreakEvents.BEFORE.register(RaidUtils::canBreak);
        UseBlockCallback.EVENT.register(RaidUtils::canPlace);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(((player, joined) ->
            NetworkMessages.sendPacketToPlayer(player, new RaidBossSyncPacket(RaidRegistry.RAID_LOOKUP.values()))
        ));

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBossReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidBucketReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidDenPoolReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidTemplateReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidTagReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new BossAdditionsReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new StatusEffectsReloadListener());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RaidSupportReloadListener());

        NetworkMessages.init();
    }

    static boolean isCobblemon171() {
        return FabricLoader.getInstance().getModContainer(Cobblemon.MODID)
            .map(cobblemon -> {
                    try { return cobblemon.getMetadata().getVersion().compareTo(Version.parse("1.7.1")) <= 0; }
                    catch (VersionParsingException e) { throw new RuntimeException(e); }
                }
            )
            .orElse(false);
    }
}
