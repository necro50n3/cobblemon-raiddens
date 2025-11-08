package com.necro.raid.dens.neoforge.events.reloader;

import com.necro.raid.dens.common.reloaders.RaidBucketReloadImpl;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

public class RaidBucketReloadListener extends RaidBucketReloadImpl implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        this.load(manager);
    }
}
