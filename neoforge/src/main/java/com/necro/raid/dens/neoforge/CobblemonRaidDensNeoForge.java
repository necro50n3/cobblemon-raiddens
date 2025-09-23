package com.necro.raid.dens.neoforge;

import com.cobblemon.mod.common.api.Priority;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientRaidBoss;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.events.RaidEvents;
import com.necro.raid.dens.common.network.JoinRaidPacket;
import com.necro.raid.dens.common.network.SyncHealthPacket;
import com.necro.raid.dens.common.network.SyncRaidDimensionsPacket;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidBuilder;
import com.necro.raid.dens.common.util.RaidBucket;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
import com.necro.raid.dens.neoforge.advancements.NeoForgeCriteriaTriggers;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlockEntities;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlocks;
import com.necro.raid.dens.neoforge.compat.distanthorizons.NeoForgeDistantHorizonsCompat;
import com.necro.raid.dens.neoforge.compat.megashowdown.NeoForgeMSDCompat;
import com.necro.raid.dens.neoforge.components.NeoForgeComponents;
import com.necro.raid.dens.neoforge.dimensions.NeoForgeChunkGenerator;
import com.necro.raid.dens.neoforge.events.CommandsRegistrationEvent;
import com.necro.raid.dens.neoforge.loot.NeoForgeLootFunctions;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import com.necro.raid.dens.neoforge.items.*;
import com.necro.raid.dens.common.util.RaidRegistry;
import com.necro.raid.dens.neoforge.statistics.NeoForgeStatistics;
import com.necro.raid.dens.neoforge.worldgen.NeoForgeFeatures;
import kotlin.Unit;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@Mod(CobblemonRaidDens.MOD_ID)
public class CobblemonRaidDensNeoForge {
    public CobblemonRaidDensNeoForge(IEventBus modBus, ModContainer container) {
        CobblemonRaidDens.init();

        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(ModList.get().isLoaded(mod.getModid()));
        }
        if (ModCompat.MEGA_SHOWDOWN.isLoaded()) NeoForgeMSDCompat.init();
        if (ModCompat.DISTANT_HORIZONS.isLoaded()) NeoForgeDistantHorizonsCompat.init();

        NeoForgeBlocks.registerModBlocks();
        NeoForgeBlocks.BLOCKS.register(modBus);
        NeoForgeBlockEntities.BLOCK_ENTITIES.register(modBus);
        NeoForgeItems.registerItems();
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgePredicates.registerPredicates();
        NeoForgePredicates.PREDICATES.register(modBus);
        NeoForgeComponents.registerDataComponents();
        NeoForgeComponents.DATA_COMPONENT_TYPES.register(modBus);
        NeoForgeChunkGenerator.CHUNK_GENERATORS.register(modBus);
        NeoForgeFeatures.registerFeatures();
        NeoForgeFeatures.FEATURES.register(modBus);
        NeoForgeLootFunctions.registerLootFunctions();
        NeoForgeLootFunctions.LOOT_FUNCTION_TYPES.register(modBus);
        NeoForgeStatistics.registerStatistics();
        NeoForgeStatistics.CUSTOM_STATS.register(modBus);
        NeoForgeCriteriaTriggers.registerCriteriaTriggers();
        NeoForgeCriteriaTriggers.TRIGGERS.register(modBus);
        RaidDenTab.CREATIVE_TABS.register(modBus);

        NeoForge.EVENT_BUS.addListener(CommandsRegistrationEvent::registerCommands);
        modBus.addListener((DataPackRegistryEvent.NewRegistry event) -> {
            event.dataPackRegistry(RaidRegistry.RAID_BOSS_KEY, RaidBoss.codec(), ClientRaidBoss.codec());
            event.dataPackRegistry(RaidBucketRegistry.BUCKET_KEY, RaidBucket.codec(), null);
        });

        RaidBuilder.SYNC_HEALTH = (player, healthRatio) ->
            NetworkMessages.sendPacketToPlayer(player, new SyncHealthPacket(healthRatio));
        DimensionHelper.SYNC_DIMENSIONS = (server, levelKey, create) ->
            NetworkMessages.sendPacketToAll(new SyncRaidDimensionsPacket(levelKey, create));

        RaidEvents.RAID_JOIN.subscribe(Priority.NORMAL, event -> {
            NetworkMessages.sendPacketToPlayer(event.getPlayer(), new JoinRaidPacket(true));
            return Unit.INSTANCE;
        });
    }
}
