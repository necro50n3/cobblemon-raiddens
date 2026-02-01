package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.packets.RaidBossSyncPacket;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.neoforge.network.NetworkMessages;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {
    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Post event) {
        RaidJoinHelper.serverTick();
    }

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) return;
        NetworkMessages.sendPacketToPlayer(event.getPlayer(), new RaidBossSyncPacket(RaidRegistry.RAID_LOOKUP.values()));
    }
}
