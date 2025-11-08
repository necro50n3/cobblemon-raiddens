package com.necro.raid.dens.neoforge.events.reloader;

import com.necro.raid.dens.common.reloaders.BossAdditionsReloadImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class BossAdditionsReloadListener extends BossAdditionsReloadImpl implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        this.load(manager);
    }
}
