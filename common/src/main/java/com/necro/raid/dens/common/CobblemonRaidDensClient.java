package com.necro.raid.dens.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.config.ClientConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class CobblemonRaidDensClient {
    public static ClientConfig CLIENT_CONFIG;

    public static void init() {
        AutoConfig.register(ClientConfig.class, JanksonConfigSerializer::new);
        CLIENT_CONFIG = AutoConfig.getConfigHolder(ClientConfig.class).getConfig();

        PlatformEvents.CLIENT_PLAYER_LOGOUT.subscribe(Priority.NORMAL, event -> {
            RaidDenGuiManager.OVERLAY_QUEUE.clear();
            RaidDenGuiManager.RAID_OVERLAY = null;
        });
    }
}
