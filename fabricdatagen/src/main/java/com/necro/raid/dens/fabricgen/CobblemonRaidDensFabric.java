package com.necro.raid.dens.fabricgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.fabricgen.blocks.FabricBlocks;
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
        FabricItems.registerItems();
        FabricPredicates.registerPredicates();
        FabricFeatures.registerFeatures();
        FabricLootFunctions.registerLootFunctions();
    }

}
