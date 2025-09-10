package com.necro.raid.dens.neoforge.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RaidBossReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        RaidRegistry.clear();

        manager.listResources("raid/boss", path -> path.toString().endsWith(".json")).forEach((id, resource) -> {
            try (InputStream input = resource.open()) {
                JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                Optional<RaidBoss> raidBossOpt = RaidBoss.codec().decode(JsonOps.INSTANCE, jsonObject).result().map(Pair::getFirst);
                raidBossOpt.ifPresent(RaidRegistry::register);
            } catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load raid boss {}", id, e);
            }
        });

        RaidRegistry.populateWeightedList();
        RaidTier.updateRandom();
    }
}
