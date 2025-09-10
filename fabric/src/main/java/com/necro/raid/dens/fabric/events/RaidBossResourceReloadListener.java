package com.necro.raid.dens.fabric.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

public class RaidBossResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid/boss");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        RaidRegistry.clear();

        for(ResourceLocation id : manager.listResources("raid/boss", path -> path.getPath().endsWith(".json")).keySet()) {
            try (Stream<Resource> stream = manager.getResource(id).stream(); InputStream input = stream.findFirst().get().open()) {
                JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                Optional<Pair<RaidBoss, JsonElement>> result = RaidBoss.codec().decode(JsonOps.INSTANCE, jsonObject).result();
                if (result.isEmpty()) return;
                RaidBoss raidBoss = result.get().getFirst();
                RaidRegistry.register(raidBoss);
            } catch(Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load raid boss {}", id, e);
            }
        }

        RaidRegistry.populateWeightedList();
        RaidTier.updateRandom();
    }
}
