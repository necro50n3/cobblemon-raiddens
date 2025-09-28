package com.necro.raid.dens.neoforge;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.neoforge.config.ClientConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@SuppressWarnings("unused")
@Mod(value = CobblemonRaidDens.MOD_ID, dist = Dist.CLIENT)
public class CobblemonRaidDensNeoForgeClient {
    public CobblemonRaidDensNeoForgeClient(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ClientConfigScreen::create);
    }
}
