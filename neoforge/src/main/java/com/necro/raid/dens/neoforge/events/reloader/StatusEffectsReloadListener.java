package com.necro.raid.dens.neoforge.events.reloader;

import com.necro.raid.dens.common.reloaders.StatusEffectsReloadImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class StatusEffectsReloadListener extends StatusEffectsReloadImpl implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        this.load(manager);
    }
}
