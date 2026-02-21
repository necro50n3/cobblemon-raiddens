package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.client.keybind.RaidDenKeybinds;
import com.necro.raid.dens.common.client.tooltip.ProgressTooltip;
import com.necro.raid.dens.common.client.tooltip.ProgressTooltipData;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
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

    @SubscribeEvent
    public static void onGatherTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ProgressTooltipData.class, data -> new ProgressTooltip(data.progress(), data.total()));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            ItemProperties.register(
                ModItems.RAID_SHARD.value(),
                ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_energy"),
                ((itemStack, level, entity, seed) -> {
                    float energy = itemStack.getOrDefault(ModComponents.RAID_ENERGY.value(), 0);
                    return energy / ((float) CobblemonRaidDens.CONFIG.required_energy);
                })
            )
        );
    }
}
