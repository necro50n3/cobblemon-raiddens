package com.necro.raid.dens.neoforge.showdown.loader;

import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.showdown.loader.ShowdownLoader;
import net.neoforged.fml.loading.LoadingModList;

public class NeoForgeShowdownLoader extends ShowdownLoader {
    @Override
    protected boolean isMegaShowdownLoaded() {
        return LoadingModList.get().getModFileById(ModCompat.MEGA_SHOWDOWN.getModid()) != null;
    }
}
