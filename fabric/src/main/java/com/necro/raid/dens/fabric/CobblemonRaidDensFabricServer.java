package com.necro.raid.dens.fabric;

import com.necro.raid.dens.fabric.events.ModEvents;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CobblemonRaidDensFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::serverTick);
    }
}
