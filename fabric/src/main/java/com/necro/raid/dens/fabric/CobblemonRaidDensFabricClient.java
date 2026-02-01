package com.necro.raid.dens.fabric;

import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.client.ClientHud;
import com.necro.raid.dens.common.client.block.RaidCrystalRenderer;
import com.necro.raid.dens.common.client.block.RaidHomeRenderer;
import com.necro.raid.dens.common.client.tooltip.ProgressTooltip;
import com.necro.raid.dens.common.client.tooltip.ProgressTooltipData;
import com.necro.raid.dens.fabric.client.FabricHud;
import com.necro.raid.dens.fabric.client.keybind.FabricKeybinds;
import com.necro.raid.dens.fabric.events.ModEvents;
import com.necro.raid.dens.fabric.network.NetworkMessages;
import com.necro.raid.dens.fabric.blocks.FabricBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class CobblemonRaidDensFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CobblemonRaidDensClient.init();
        NetworkMessages.registerS2CPackets();

        ClientHud.init();
        FabricKeybinds.registerKeybinds();

        BlockEntityRenderers.register(FabricBlocks.RAID_CRYSTAL_BLOCK_ENTITY, RaidCrystalRenderer::new);
        BlockEntityRenderers.register(FabricBlocks.RAID_HOME_BLOCK_ENTITY, RaidHomeRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(ModEvents::clientTick);
        HudRenderCallback.EVENT.register(new FabricHud());
        TooltipComponentCallback.EVENT.register(component -> component instanceof ProgressTooltipData(double progress, double total) ? new ProgressTooltip(progress, total) : null);
    }
}
