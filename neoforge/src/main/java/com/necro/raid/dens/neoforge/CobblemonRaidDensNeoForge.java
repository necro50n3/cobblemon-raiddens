package com.necro.raid.dens.neoforge;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.neoforge.advancements.NeoForgeCriteriaTriggers;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlockEntities;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlocks;
import com.necro.raid.dens.neoforge.components.NeoForgeComponents;
import com.necro.raid.dens.neoforge.dimensions.NeoForgeDimensions;
import com.necro.raid.dens.neoforge.events.CommandsRegistrationEvent;
import com.necro.raid.dens.neoforge.loot.NeoForgeLootFunctions;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import com.necro.raid.dens.neoforge.items.*;
import com.necro.raid.dens.neoforge.statistics.NeoForgeStatistics;
import com.necro.raid.dens.neoforge.worldgen.NeoForgeFeatures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@SuppressWarnings("unused")
@Mod(CobblemonRaidDens.MOD_ID)
public class CobblemonRaidDensNeoForge {
    public CobblemonRaidDensNeoForge(IEventBus modBus, ModContainer container) {
        CobblemonRaidDens.init();

        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(ModList.get().isLoaded(mod.getModid()));
        }

        NeoForgeBlocks.registerModBlocks();
        NeoForgeBlocks.BLOCKS.register(modBus);
        NeoForgeBlockEntities.BLOCK_ENTITIES.register(modBus);
        NeoForgeItems.registerItems();
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgePredicates.registerPredicates();
        NeoForgePredicates.PREDICATES.register(modBus);
        NeoForgeComponents.registerDataComponents();
        NeoForgeComponents.DATA_COMPONENT_TYPES.register(modBus);
        NeoForgeDimensions.CHUNK_GENERATORS.register(modBus);
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

        NetworkMessages.init();
    }
}
