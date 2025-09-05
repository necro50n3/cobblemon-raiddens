package com.necro.raid.dens.fabric;

import com.necro.raid.dens.fabric.events.ModEvents;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class CobblemonRaidDensFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEvents::onServerClose);
        ServerTickEvents.END_SERVER_TICK.register(ModEvents::serverTick);
    }
}
