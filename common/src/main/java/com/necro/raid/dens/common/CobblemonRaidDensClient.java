package com.necro.raid.dens.common;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.config.ClientConfig;
import com.necro.raid.dens.common.items.ModItems;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class CobblemonRaidDensClient {
    public static ClientConfig CLIENT_CONFIG;

    public static void init() {
        AutoConfig.register(ClientConfig.class, JanksonConfigSerializer::new);
        CLIENT_CONFIG = AutoConfig.getConfigHolder(ClientConfig.class).getConfig();

        PlatformEvents.CLIENT_PLAYER_LOGOUT.subscribe(Priority.NORMAL, event -> {
            RaidDenGuiManager.OVERLAY_QUEUE.clear();
            RaidDenGuiManager.RAID_OVERLAY = null;
        });
        ItemProperties.register(
            ModItems.RAID_SHARD.value(),
            ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_energy"),
            ((itemStack, level, entity, seed) -> {
                float energy = itemStack.getOrDefault(ModComponents.RAID_ENERGY.value(), 0);
                return energy / ((float) CobblemonRaidDens.CONFIG.required_energy);
            })
        );
    }
}
