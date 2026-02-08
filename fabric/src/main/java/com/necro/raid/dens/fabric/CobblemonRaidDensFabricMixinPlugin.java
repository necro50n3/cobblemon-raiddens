package com.necro.raid.dens.fabric;

import com.necro.raid.dens.common.CobblemonRaidDensMixinPluginImpl;
import net.fabricmc.loader.api.FabricLoader;

public class CobblemonRaidDensFabricMixinPlugin extends CobblemonRaidDensMixinPluginImpl {
    @Override
    protected String mixin(String pkg) {
        return "com.necro.raid.dens.fabric.mixins." + pkg;
    }

    @Override
    protected boolean isCobblemon171() {
        return CobblemonRaidDensFabric.isCobblemon171();
    }

    @Override
    protected boolean isModLoaded(String... mods) {
        for (String mod : mods) {
            if (FabricLoader.getInstance().isModLoaded(mod)) return true;
        }
        return false;
    }
}
