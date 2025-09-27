package com.necro.raid.dens.fabric.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.util.Optional;

public class RaidTemplateResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid/structure");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        RaidDenRegistry.clear();

        for(ResourceLocation id : manager.listResources("structure/raid_den", path -> path.getPath().endsWith(".nbt")).keySet()) {
            Optional<Resource> resource = manager.getResource(id);
            if (resource.isEmpty()) continue;

            try (InputStream input = resource.get().open()) {
                CompoundTag nbt = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                if (!nbt.contains("raid_pois")) continue;
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("structure/", "").replace(".nbt", ""));
                RaidDenRegistry.register(key, nbt.getCompound("raid_pois"));
            } catch(Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load raid den template {}", id, e);
            }
        }
    }
}
