package com.necro.raid.dens.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.commands.RaidAdminCommands;
import com.necro.raid.dens.common.commands.RaidRequestCommands;
import com.necro.raid.dens.common.commands.RaidRewardCommands;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import com.necro.raid.dens.fabric.components.FabricComponents;
import com.necro.raid.dens.fabric.items.FabricItems;
import com.necro.raid.dens.fabric.items.FabricPredicates;
import com.necro.raid.dens.fabric.items.RaidDenTab;
import com.necro.raid.dens.common.network.SyncHealthPacket;
import com.necro.raid.dens.common.raids.RaidBuilder;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.fabric.dimensions.FabricDimensions;
import com.necro.raid.dens.fabric.events.ModEvents;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import com.necro.raid.dens.fabric.worldgen.FabricFeatures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

public class CobblemonRaidDensFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CobblemonRaidDens.init();
        NetworkMessages.registerPayload();
        FabricBlocks.registerModBlocks();
        FabricItems.registerItems();
        FabricDimensions.registerChunkGenerator();
        FabricComponents.registerDataComponents();
        FabricPredicates.registerPredicates();
        FabricFeatures.registerFeatures();
        RaidDenTab.registerItemGroups();

        ServerLifecycleEvents.SERVER_STARTED.register(ModEvents::initRaidHelper);
        ServerPlayConnectionEvents.DISCONNECT.register(ModEvents::onPlayerDisconnect);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::commonTick);
        ServerTickEvents.END_SERVER_TICK.register(DimensionHelper::removePending);
        CommandRegistrationCallback.EVENT.register(RaidAdminCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidRequestCommands::register);
        CommandRegistrationCallback.EVENT.register(RaidRewardCommands::register);
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(DimensionHelper::onDimensionChange);

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_bosses");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                for(ResourceLocation id : manager.listResources("raid/boss", path -> path.getPath().endsWith(".json")).keySet()) {
                    try (Stream<Resource> stream = manager.getResource(id).stream(); InputStream input = stream.findFirst().get().open()) {
                        JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8));
                        Optional<Pair<RaidBoss, JsonElement>> result = RaidBoss.codec().decode(JsonOps.INSTANCE, jsonObject).result();
                        if (result.isEmpty()) return;
                        RaidBoss raidBoss = result.get().getFirst();
                        RaidRegistry.register(raidBoss);
                    } catch(Exception e) {
                        CobblemonRaidDens.LOGGER.error("Error occurred while loading resource json: {}", id.toString(), e);
                    }
                }
                RaidRegistry.populateWeightedList();
                RaidTier.updateRandom();
            }
        });

        RaidBuilder.SYNC_HEALTH = (player, healthRatio) ->
            NetworkMessages.sendPacketToPlayer(player, new SyncHealthPacket(healthRatio));

        for (ModCompat mod : ModCompat.values()) {
            mod.setLoaded(FabricLoader.getInstance().isModLoaded(mod.getModid()));
        }
    }

}
