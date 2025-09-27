package com.necro.raid.dens.neoforge.events.reloader;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class RaidTemplateReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        RaidDenRegistry.clear();

        manager.listResources("structure/raid_den", path -> path.toString().endsWith(".nbt")).forEach((id, resource) -> {
            try (InputStream input = resource.open()) {
                CompoundTag nbt = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                if (!nbt.contains("raid_pois")) return;
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("structure/", "").replace(".nbt", ""));
                RaidDenRegistry.register(key, nbt.getCompound("raid_pois"));
            } catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load raid den template {}", id, e);
            }
        });
    }
}
