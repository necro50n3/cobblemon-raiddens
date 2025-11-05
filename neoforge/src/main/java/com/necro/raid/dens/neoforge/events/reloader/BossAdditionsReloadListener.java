package com.necro.raid.dens.neoforge.events.reloader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidBossAdditions;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BossAdditionsReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(@NotNull ResourceManager manager) {
        List<ResourceLocation> baseList = new ArrayList<>(RaidRegistry.getAll());

        manager.listResources("raid/boss_additions", path -> path.toString().endsWith(".json")).forEach((id, resource) -> {
            try (InputStream input = resource.open()) {
                JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                Optional<RaidBossAdditions> additionsOpt = RaidBossAdditions.codec().decode(JsonOps.INSTANCE, jsonObject).result().map(Pair::getFirst);
                additionsOpt.ifPresent((additions) -> additions.apply(baseList));
            } catch (Exception e) {
                CobblemonRaidDens.LOGGER.error("Failed to load boss additions {}", id, e);
            }
        });

        RaidRegistry.registerAll();
        RaidTier.updateRandom();
    }
}
