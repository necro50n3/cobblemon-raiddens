package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.block.RaidCrystalRenderer;
import com.necro.raid.dens.common.client.block.RaidHomeRenderer;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlockEntities;
import com.necro.raid.dens.neoforge.blocks.NeoForgeBlocks;
import com.necro.raid.dens.neoforge.client.NeoForgeHud;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistrationEvents {
    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(NeoForgeBlockEntities.RAID_CRYSTAL_BLOCK_ENTITY.get(), RaidCrystalRenderer::new);
        event.registerBlockEntityRenderer(NeoForgeBlockEntities.RAID_HOME_BLOCK_ENTITY.get(), RaidHomeRenderer::new);
    }

    @SubscribeEvent
    public static void addCreativeModeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(NeoForgeBlocks.RAID_CRYSTAL_BLOCK.get());
        }
    }

    @SubscribeEvent
    public static  void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(RaidDenKeybinds.MOUSE_KEYDOWN);
        event.register(RaidDenKeybinds.ACCEPT_SHORTCUT);
        event.register(RaidDenKeybinds.DENY_SHORTCUT);

        NeoForgeHud.init();
    }
}
