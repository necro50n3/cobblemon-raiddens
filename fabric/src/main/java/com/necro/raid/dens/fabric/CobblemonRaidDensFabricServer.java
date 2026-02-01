package com.necro.raid.dens.fabric;

import com.necro.raid.dens.common.network.packets.RaidBossSyncPacket;
import com.necro.raid.dens.common.registry.RaidRegistry;
import com.necro.raid.dens.fabric.events.ModEvents;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CobblemonRaidDensFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::serverTick);

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(((player, joined) -> {
            if (joined) NetworkMessages.sendPacketToPlayer(player, new RaidBossSyncPacket(RaidRegistry.RAID_LOOKUP.values()));
        }));
    }
}
