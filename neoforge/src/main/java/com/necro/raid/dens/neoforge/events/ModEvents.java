package com.necro.raid.dens.neoforge.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.dimensions.ModDimensions;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.party.RaidStoreFactory;
import com.necro.raid.dens.common.raids.RaidSession;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.showdown.events.RaidEvents;
import com.necro.raid.dens.common.util.RaidUtils;
import com.necro.raid.dens.neoforge.events.reloader.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.nio.file.Path;
import java.util.Optional;

@EventBusSubscriber(modid = CobblemonRaidDens.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    private static void commonTick(ServerTickEvent.Post event) {
        RaidHelper.commonTick(event.getServer());
        RaidJoinHelper.serverTick();
        RaidEvents.ScaleBossRaidEvent.tick();
        CustomRaidRegistries.RAID_REGISTRY.tick();
    }

    @SubscribeEvent
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getServer() == null) return;
        RaidDenNetworkMessages.SYNC_CONFIG.accept(player);
        player.getServer().execute(() -> {
            if (RaidJoinHelper.isParticipatingOrInQueue(player, false)) {
                RaidDenNetworkMessages.JOIN_RAID.accept(player, true);
            }
            if (RaidHelper.REWARD_QUEUE.containsKey(player.getUUID())) RaidHelper.REWARD_QUEUE.get(player.getUUID()).sendRewardMessage(player);
        });
    }

    @SubscribeEvent
    private static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.getServer() == null) return;
        player.getServer().execute(() -> RaidJoinHelper.onPlayerDisconnect(player));
    }

    @SubscribeEvent
    private static void onServerStarting(ServerStartingEvent event) {
        CustomRaidRegistries.freeze();
        CustomRaidRegistries.RAID_REGISTRY.init(event.getServer());
        Cobblemon.INSTANCE.getStorage().registerFactory(Priority.NORMAL, new RaidStoreFactory());
    }

    @SubscribeEvent
    private static void onServerStarted(ServerStartedEvent event) {
        RaidHelper.initHelper(event.getServer());
    }

    @SubscribeEvent
    private static void onServerClose(ServerStoppingEvent event) {
        RaidJoinHelper.onServerClose();
        RaidHelper.onServerClose(event.getServer());
        CustomRaidRegistries.RAID_REGISTRY.close();
    }

    @SubscribeEvent
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (RaidUtils.cannotPlace(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (RaidUtils.cannotBreak(event.getPlayer(), (Level) event.getLevel())) event.setCanceled(true);
    }

    @SubscribeEvent
    private static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (cancelUseItem(event)) event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent
    private static void onUseItemOnEntity(PlayerInteractEvent.EntityInteract event) {
        if (cancelUseItem(event)) event.setCancellationResult(InteractionResult.FAIL);
    }

    private static boolean cancelUseItem(PlayerInteractEvent event) {
        ItemStack stack = event.getEntity().getItemInHand(event.getHand());
        if (event.getEntity() instanceof LocalPlayer) return false;
        if (!(stack.getItem() instanceof HealingSource)) return false;
        RaidJoinHelper.Participant participant = RaidJoinHelper.getParticipant(event.getEntity());
        if (participant == null) return false;
        RaidSession session = RaidHelper.ACTIVE_RAIDS.get(participant.raid());
        if (session == null || session.canUseHealingItems((ServerPlayer) event.getEntity())) return false;
        event.getEntity().displayClientMessage(Component.translatable("cobblemon.battle.bagitem.cannot"), true);
        return true;
    }

    @SubscribeEvent
    private static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;
        ModDimensions.onDimensionChange((ServerPlayer) event.getEntity(), server.getLevel(event.getFrom()), server.getLevel(event.getTo()));
    }

    @SubscribeEvent
    private static void onReloadDataPack(AddReloadListenerEvent event) {
        event.addListener(new RaidBossReloadListener());
        event.addListener(new RaidBucketReloadListener());
        event.addListener(new RaidDenPoolReloadListener());
        event.addListener(new RaidTemplateReloadListener());
        event.addListener(new RaidTagReloadListener());
        event.addListener(new BossAdditionsReloadListener());
        event.addListener(new StatusEffectsReloadListener());
        event.addListener(new RaidSupportReloadListener());
    }

    @SubscribeEvent
    private static void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) return;
        RaidDenNetworkMessages.SYNC_REGISTRY.accept(event.getPlayer());
    }

    @SubscribeEvent
    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (ModCompat.CREATE.isLoaded()) registerDatapack(event, "raiddenscreatecompat", "Raid Dens Create Compat");
        if (ModCompat.MEKANISM.isLoaded()) registerDatapack(event, "raiddensmekanismcompat", "Raid Dens Mekanism Compat");
    }

    private static void registerDatapack(AddPackFindersEvent event, String path, String name) {
        String packLocation = "mod/" + ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, path);
        IModInfo modInfo = ModList.get().getModContainerById(CobblemonRaidDens.MOD_ID).orElseThrow().getModInfo();
        ArtifactVersion version = modInfo.getVersion();
        Path resourcePath = modInfo.getOwningFile().getFile().findResource("datapacks/" + path);
        Pack pack = Pack.readMetaAndCreate(
            new PackLocationInfo(
                packLocation,
                Component.literal(name),
                PackSource.BUILT_IN,
                Optional.of(new KnownPack("neoforge", packLocation, version.toString()))
            ),
            BuiltInPackSource.fromName(info -> new PathPackResources(info, resourcePath)),
            PackType.SERVER_DATA,
            new PackSelectionConfig(true, Pack.Position.TOP, false)
        );

        if (pack == null) return;
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }
}
