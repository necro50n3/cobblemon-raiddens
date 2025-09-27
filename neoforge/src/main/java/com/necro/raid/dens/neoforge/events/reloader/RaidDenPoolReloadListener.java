package com.necro.raid.dens.neoforge.events.reloader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.structure.RaidDenPool;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RaidDenPoolReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        RaidBucketRegistry.clear();

        manager.listResources("raid/den_pool", path -> path.toString().endsWith(".json")).forEach((id, resource) -> {
            try (InputStream input = resource.open()) {
                JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                ResourceLocation key = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("raid/den_pool/", "").replace(".json", ""));
                Optional<RaidDenPool> denOpt = RaidDenPool.codec().decode(JsonOps.INSTANCE, jsonObject).result().map(Pair::getFirst);
                denOpt.ifPresent(denPool -> {
                    denPool.setId(key);
                    RaidDenRegistry.register(denPool);
                });
            } catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load raid den pool {}", id, e);
            }
        });
    }
}
