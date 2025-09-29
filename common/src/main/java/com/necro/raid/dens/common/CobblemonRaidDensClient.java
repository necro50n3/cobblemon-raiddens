package com.necro.raid.dens.common;

import com.necro.raid.dens.common.config.ClientConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class CobblemonRaidDensClient {
    public static ClientConfig CLIENT_CONFIG;

    public static void init() {
        AutoConfig.register(ClientConfig.class, JanksonConfigSerializer::new);
        CLIENT_CONFIG = AutoConfig.getConfigHolder(ClientConfig.class).getConfig();
    }
}
