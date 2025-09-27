package com.necro.raid.dens.fabric.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.structure.RaidDenPool;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

public class RaidDenPoolResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid/den_pool");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        RaidDenRegistry.clear();

        for(ResourceLocation id : manager.listResources("raid/den_pool", path -> path.getPath().endsWith(".nbt")).keySet()) {
            try (Stream<Resource> stream = manager.getResource(id).stream(); InputStream input = stream.findFirst().get().open()) {
                JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                Optional<Pair<RaidDenPool, JsonElement>> result = RaidDenPool.codec().decode(JsonOps.INSTANCE, jsonObject).result();
                if (result.isEmpty()) continue;
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("raid/den_pool/", "").replace(".json", ""));
                RaidDenPool pool = result.get().getFirst();
                pool.setId(key);
                RaidDenRegistry.register(pool);
            } catch(Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load raid den pool {}", id, e);
            }
        }
    }
}
