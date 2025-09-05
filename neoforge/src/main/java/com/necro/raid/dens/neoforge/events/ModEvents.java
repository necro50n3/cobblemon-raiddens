package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void commonTick(ServerTickEvent.Post event) {
        RaidHelper.commonTick();
        DimensionHelper.removePending(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        RaidHelper.onPlayerDisconnect(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        RaidHelper.initHelper(event.getServer());
        event.getServer().registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY).forEach(RaidRegistry::register);
        RaidRegistry.populateWeightedList();
        RaidTier.updateRandom();
    }
}
