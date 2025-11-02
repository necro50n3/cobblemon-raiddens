package com.necro.raid.dens.fabric.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidBossAdditions;
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

public class BossAdditionsResourceReloadListener implements SimpleSynchronousResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid/boss_additions");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        for(ResourceLocation id : manager.listResources("raid/boss_additions", path -> path.getPath().endsWith(".json")).keySet()) {
            try (Stream<Resource> stream = manager.getResource(id).stream(); InputStream input = stream.findFirst().get().open()) {
                JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                Optional<Pair<RaidBossAdditions, JsonElement>> result = RaidBossAdditions.codec().decode(JsonOps.INSTANCE, jsonObject).result();
                if (result.isEmpty()) continue;
                RaidBossAdditions additions = result.get().getFirst();
                additions.apply();
            } catch(Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load boss additions {}", id, e);
            }
        }

        RaidRegistry.registerAll();
    }
}
