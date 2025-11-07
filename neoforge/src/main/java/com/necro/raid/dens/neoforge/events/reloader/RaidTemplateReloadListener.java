package com.necro.raid.dens.neoforge.events.reloader;

import com.necro.raid.dens.common.reloaders.RaidTemplateReloadImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class RaidTemplateReloadListener extends RaidTemplateReloadImpl implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        this.load(manager);
    }
}
