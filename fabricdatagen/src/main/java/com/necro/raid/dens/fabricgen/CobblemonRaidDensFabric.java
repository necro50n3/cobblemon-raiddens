package com.necro.raid.dens.fabricgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.fabricgen.advancements.FabricCriteriaTriggers;
import com.necro.raid.dens.fabricgen.blocks.FabricBlocks;
import com.necro.raid.dens.fabricgen.components.FabricComponents;
import com.necro.raid.dens.fabricgen.dimensions.FabricDimensions;
import com.necro.raid.dens.fabricgen.loot.FabricLootFunctions;
import com.necro.raid.dens.fabricgen.worldgen.FabricFeatures;
import com.necro.raid.dens.fabricgen.items.FabricItems;
import com.necro.raid.dens.fabricgen.items.FabricPredicates;
import net.fabricmc.api.ModInitializer;

public class CobblemonRaidDensFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobblemonRaidDens.init();
        FabricBlocks.registerModBlocks();
        FabricComponents.registerDataComponents();
        FabricItems.registerItems();
        FabricDimensions.registerChunkGenerator();
        FabricPredicates.registerPredicates();
        FabricFeatures.registerFeatures();
        FabricLootFunctions.registerLootFunctions();
        FabricCriteriaTriggers.registerCriteriaTriggers();
    }

}
