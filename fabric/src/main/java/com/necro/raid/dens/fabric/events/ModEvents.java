package com.necro.raid.dens.fabric.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.item.HealingSource;
import com.necro.raid.dens.common.client.gui.RaidDenGuiManager;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.party.RaidStoreFactory;
import com.necro.raid.dens.common.raids.RaidSession;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.raids.helpers.RaidJoinHelper;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.showdown.events.RaidEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class ModEvents {
    public static void commonTick(MinecraftServer server) {
        RaidHelper.commonTick(server);
        RaidJoinHelper.serverTick();
        RaidEvents.ScaleBossRaidEvent.tick();
        CustomRaidRegistries.RAID_REGISTRY.tick();
    }

    public static void clientTick(Minecraft client) {
        RaidDenGuiManager.tick();
    }

    public static void onPlayerJoin(ServerGamePacketListenerImpl listener, PacketSender sender, MinecraftServer server) {
        ServerPlayer player = listener.getPlayer();
        RaidDenNetworkMessages.SYNC_CONFIG.accept(player);
        server.execute(() -> {
            if (RaidJoinHelper.isParticipatingOrInQueue(player, false)) {
                RaidDenNetworkMessages.JOIN_RAID.accept(player, true);
            }
            if (RaidHelper.REWARD_QUEUE.containsKey(player.getUUID())) RaidHelper.REWARD_QUEUE.get(player.getUUID()).sendRewardMessage(player);
        });
    }

    public static void onPlayerDisconnect(ServerGamePacketListenerImpl listener, MinecraftServer server) {
        ServerPlayer player = listener.getPlayer();
        server.execute(() -> RaidJoinHelper.onPlayerDisconnect(player));
    }

    public static void onServerStarting(MinecraftServer server) {
        CustomRaidRegistries.freeze();
        CustomRaidRegistries.RAID_REGISTRY.init(server);
        Cobblemon.INSTANCE.getStorage().registerFactory(Priority.NORMAL, new RaidStoreFactory());
    }

    public static void onServerStarted(MinecraftServer server) {
        RaidHelper.initHelper(server);
    }

    public static void onServerClose(MinecraftServer server) {
        RaidJoinHelper.onServerClose();
        RaidHelper.onServerClose(server);
        CustomRaidRegistries.RAID_REGISTRY.close();
    }

    public static InteractionResultHolder<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof LocalPlayer) return InteractionResultHolder.pass(stack);
        if (!(stack.getItem() instanceof HealingSource)) return InteractionResultHolder.pass(stack);
        RaidJoinHelper.Participant participant = RaidJoinHelper.getParticipant(player);
        if (participant == null) return InteractionResultHolder.pass(stack);
        RaidSession session = RaidHelper.ACTIVE_RAIDS.get(participant.raid());
        if (session == null || session.canUseHealingItems((ServerPlayer) player)) return InteractionResultHolder.pass(stack);
        player.displayClientMessage(Component.translatable("cobblemon.battle.bagitem.cannot"), true);
        return InteractionResultHolder.fail(stack);
    }
}
