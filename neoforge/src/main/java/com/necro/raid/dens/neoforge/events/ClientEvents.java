package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.ClientManager;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        ClientManager.clientTick();
        RaidDenGuiManager.tick();
    }

    @SubscribeEvent
    public static void keydownTick(ClientTickEvent.Post event) {
        RaidDenKeybinds.handleKeyInput();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        if (!RaidDenGuiManager.hasOverlay()) return;

        Minecraft client = Minecraft.getInstance();
        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();
        RaidDenGuiManager.render(event.getGuiGraphics(), width, height, event.getPartialTick().getGameTimeDeltaTicks());
    }
}
