package com.necro.raid.dens.common.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.necro.raid.dens.common.CobblemonRaidDens;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class ClientManager {
    public static final List<Callable<Boolean>> RAID_INSTRUCTION_QUEUE = new ArrayList<>();

    public static void clientTick() {
        if (RAID_INSTRUCTION_QUEUE.isEmpty()) return;
        else if (CobblemonClient.INSTANCE.getBattle() == null) return;
        Iterator<Callable<Boolean>> iter = RAID_INSTRUCTION_QUEUE.iterator();
        try {
            while (iter.hasNext()) {
                if (iter.next().call()) iter.remove();
            }
        }
        catch (Exception e) {
            CobblemonRaidDens.LOGGER.info("Error during client tick: {}", e.toString());
        }
    }
}
