package com.necro.raid.dens.fabricgen;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.fabricgen.advancements.FabricCriteriaTriggers;
import com.necro.raid.dens.fabricgen.blocks.FabricBlocks;
import com.necro.raid.dens.fabricgen.components.FabricComponents;
import com.necro.raid.dens.fabricgen.dimensions.FabricDimensions;
import com.necro.raid.dens.fabricgen.loot.FabricLootConditions;
import com.necro.raid.dens.fabricgen.loot.FabricLootFunctions;
import com.necro.raid.dens.fabricgen.worldgen.FabricFeatures;
import com.necro.raid.dens.fabricgen.items.FabricItems;
import com.necro.raid.dens.fabricgen.items.FabricPredicates;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class CobblemonRaidDensFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModCompat.setComparator((actual, compare) -> {
            try { return Version.parse(actual).compareTo(Version.parse(compare)); }
            catch (VersionParsingException e) { throw new RuntimeException(e); }
        });
        for (ModCompat mod : ModCompat.values()) {
            FabricLoader.getInstance().getModContainer(mod.getModid()).ifPresent(modContainer -> {
                mod.setLoaded(true);
                mod.setVersion(modContainer.getMetadata().getVersion().getFriendlyString());
            });
        }

        CobblemonRaidDens.init();

        FabricBlocks.registerBlocks();
        FabricComponents.registerDataComponents();
        FabricItems.registerItems();
        FabricDimensions.registerChunkGenerator();
        FabricPredicates.registerPredicates();
        FabricFeatures.registerFeatures();
        FabricLootConditions.registerLootConditions();
        FabricLootFunctions.registerLootFunctions();
        FabricCriteriaTriggers.registerCriteriaTriggers();
    }

}
