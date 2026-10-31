package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.ModBlocks;
import com.necro.raid.dens.common.client.ClientHud;
import com.necro.raid.dens.common.client.block.RaidHomeRenderer;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.neoforge.client.block.RaidCrystalRendererNeoForge;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID, value = Dist.CLIENT)
public class ClientRegistrationEvents {
    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.RAID_CRYSTAL_BLOCK_ENTITY.value(), RaidCrystalRendererNeoForge::new);
        event.registerBlockEntityRenderer(ModBlocks.RAID_HOME_BLOCK_ENTITY.value(), RaidHomeRenderer::new);
    }

    @SubscribeEvent
    public static void addCreativeModeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.RAID_CRYSTAL_BLOCK.value());
        }
    }

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(RaidDenKeybinds.MOUSE_KEYDOWN);
        event.register(RaidDenKeybinds.ACCEPT_SHORTCUT);
        event.register(RaidDenKeybinds.DENY_SHORTCUT);

        ClientHud.init();
    }
}
