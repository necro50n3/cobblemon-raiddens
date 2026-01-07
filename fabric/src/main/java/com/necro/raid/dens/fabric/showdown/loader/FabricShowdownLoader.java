package com.necro.raid.dens.fabric.showdown.loader;

import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.showdown.loader.ShowdownLoader;
import net.fabricmc.loader.api.FabricLoader;

public class FabricShowdownLoader extends ShowdownLoader {
    @Override
    protected boolean isMegaShowdownLoaded() {
        return FabricLoader.getInstance().isModLoaded(ModCompat.MEGA_SHOWDOWN.getModid());
    }
}
