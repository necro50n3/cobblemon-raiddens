package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.RaidExitHelper;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
import com.necro.raid.dens.common.util.RaidRegistry;
import com.necro.raid.dens.common.util.RaidUtils;
import com.necro.raid.dens.neoforge.events.reloader.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void commonTick(ServerTickEvent.Post event) {
        RaidHelper.commonTick();
        DimensionHelper.removePending(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (RaidHelper.isAlreadyHosting(player) || RaidHelper.isAlreadyParticipating(player) || RaidHelper.JOIN_QUEUE.containsKey(player)) {
            RaidDenNetworkMessages.JOIN_RAID.accept(player, true);
        }
        if (RaidHelper.REWARD_QUEUE.containsKey(player.getUUID())) RaidHelper.REWARD_QUEUE.get(player.getUUID()).sendRewardMessage();
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        RaidHelper.onPlayerDisconnect(event.getEntity());
        DimensionHelper.removeDelayed(event.getEntity().getServer(), (ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        RaidHelper.initHelper(server);
        RaidRegistry.initRaidBosses(server);
        RaidBucketRegistry.init(server);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        DimensionHelper.removeDelayed(event.getServer());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (RaidUtils.cannotPlace(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (RaidUtils.cannotBreak(event.getPlayer(), (Level) event.getLevel())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onReloadDataPack(AddReloadListenerEvent event) {
        event.addListener(new RaidBossReloadListener());
        event.addListener(new RaidBucketReloadListener());
        event.addListener(new RaidDenPoolReloadListener());
        event.addListener(new RaidTemplateReloadListener());
        event.addListener(new BossAdditionsReloadListener());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
            RaidExitHelper.onDimensionChange(serverPlayer, event.getFrom(), event.getTo());
    }
}
