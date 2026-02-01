package com.necro.raid.dens.neoforge.events;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
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
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (RaidJoinHelper.isParticipatingOrInQueue(player, false)) {
            RaidDenNetworkMessages.JOIN_RAID.accept(player, true);
        }
        if (RaidHelper.REWARD_QUEUE.containsKey(player.getUUID())) RaidHelper.REWARD_QUEUE.get(player.getUUID()).sendRewardMessage();
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        RaidJoinHelper.onPlayerDisconnect(event.getEntity());
        RaidHelper.onPlayerDisconnect((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        RaidHelper.initHelper(server);

        RaidBucketRegistry.init(server);
    }

    @SubscribeEvent
    public static void onServerClose(ServerStoppingEvent event) {
        RaidJoinHelper.onServerClose();
        RaidHelper.onServerClose(event.getServer());
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
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        ModDimensions.onDimensionChange((ServerPlayer) event.getEntity(), server.getLevel(event.getFrom()), server.getLevel(event.getTo()));
    }

    @SubscribeEvent
    public static void onReloadDataPack(AddReloadListenerEvent event) {
        event.addListener(new RaidBossReloadListener());
        event.addListener(new RaidBucketReloadListener());
        event.addListener(new RaidDenPoolReloadListener());
        event.addListener(new RaidTemplateReloadListener());
        event.addListener(new RaidTagReloadListener());
        event.addListener(new BossAdditionsReloadListener());
        event.addListener(new StatusEffectsReloadListener());
        event.addListener(new RaidSupportReloadListener());
    }
}
