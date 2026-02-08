package com.necro.raid.dens.neoforge;

import com.necro.raid.dens.common.CobblemonRaidDensMixinPluginImpl;
import net.neoforged.fml.loading.LoadingModList;

public class CobblemonRaidDensNeoForgeMixinPlugin extends CobblemonRaidDensMixinPluginImpl {
    @Override
    protected String mixin(String pkg) {
        return "com.necro.raid.dens.neoforge.mixins." + pkg;
    }

    @Override
    protected boolean isCobblemon171() {
        return CobblemonRaidDensNeoForge.isCobblemon171();
    }

    @Override
    protected boolean isModLoaded(String... mods) {
        for (String mod : mods) {
            if (LoadingModList.get().getModFileById(mod) != null) return true;
        }
        return false;
    }
}
