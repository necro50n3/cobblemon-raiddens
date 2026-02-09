package com.necro.raid.dens.fabric.events.reloader;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.reloaders.RaidDenPoolReloadImpl;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class RaidDenPoolReloadListener extends RaidDenPoolReloadImpl implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid/den_pool");
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        this.load(manager);
    }
}
