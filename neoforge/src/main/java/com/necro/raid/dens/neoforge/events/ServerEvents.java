package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerClose(ServerStoppingEvent event) {
        RaidHelper.onServerClose();
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        RaidHelper.serverTick();
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        DimensionHelper.onDimensionChange((ServerPlayer) event.getEntity(), event.getFrom(), event.getTo());
    }
}
