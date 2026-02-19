package com.necro.raid.dens.common.raids.helpers;

import com.necro.raid.dens.common.util.ComponentUtils;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidJoinHelper {
    private static final Map<Player, JoinRequest> JOIN_QUEUE = new HashMap<>();
    private static final Map<UUID, Participant> RAID_PARTICIPANTS = new HashMap<>();

    public static boolean isInQueue(Player player) {
        return JOIN_QUEUE.containsKey(player);
    }

    public static void addToQueue(Player player, @Nullable ItemStack key) {
        JOIN_QUEUE.put(player, new JoinRequest(player, key));
    }

    public static void removeFromQueue(Player player, boolean refund) {
        JoinRequest joinRequest = JOIN_QUEUE.remove(player);
        if (joinRequest == null) return;
        if (refund) joinRequest.refundItem();
    }

    public static boolean addParticipant(Player player, UUID raid, boolean isHost, boolean sendMessage) {
        boolean isParticipating = isParticipatingOrInQueue(player, sendMessage);
        if (!isParticipating) RAID_PARTICIPANTS.put(player.getUUID(), new Participant(raid, isHost));
        return !isParticipating;
    }

    public static Participant getParticipant(Player player) {
        return RAID_PARTICIPANTS.get(player.getUUID());
    }

    public static void removeParticipant(Player player) {
        RAID_PARTICIPANTS.remove(player.getUUID());
    }

    public static void removeParticipants(Collection<? extends Player> players) {
        players.forEach(player -> RAID_PARTICIPANTS.remove(player.getUUID()));
    }

    public static boolean isParticipating(Player player, UUID raid) {
        Participant participant = RAID_PARTICIPANTS.get(player.getUUID());
        if (participant == null) return false;
        return participant.raid().equals(raid);
    }

    public static boolean isParticipating(Player player, boolean sendMessage) {
        Participant participant = RAID_PARTICIPANTS.get(player.getUUID());
        if (participant == null) return false;

        if (participant.isHost()) {
            if (sendMessage) player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.already_hosting"), true);
        }
        else {
            if (sendMessage) player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.already_participating"), true);
        }
        return true;
    }

    public static boolean isParticipatingOrInQueue(Player player, boolean sendMessage) {
        if (isInQueue(player)) {
            if (sendMessage) player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.already_in_queue"), true);
            return true;
        }
        return isParticipating(player, sendMessage);
    }

    public static void onServerClose() {
        JOIN_QUEUE.forEach((player, instance) -> instance.refundItem());
        JOIN_QUEUE.clear();
        RAID_PARTICIPANTS.clear();
    }

    public static void serverTick() {
        JOIN_QUEUE.values().removeIf(instance -> !instance.tick());
    }

    public static void onPlayerDisconnect(Player player) {
        refundItem(player);
        RaidUtils.leaveRaid(player);
    }

    private static void refundItem(Player player) {
        if (!JOIN_QUEUE.containsKey(player)) return;
        JOIN_QUEUE.get(player).refundItem();
        JOIN_QUEUE.remove(player);
    }

    public record Participant(UUID raid, boolean isHost) {}

    public static class JoinRequest {
        private final Player player;
        private final ItemStack itemStack;
        private int tick;

        public JoinRequest(Player player, @Nullable ItemStack itemStack) {
            this.player = player;
            if (itemStack == null) this.itemStack = null;
            else {
                this.itemStack = itemStack.copy();
                this.itemStack.setCount(1);
            }
            this.tick = 0;
        }

        public void refundItem() {
            if (this.itemStack != null) player.addItem(this.itemStack);
        }

        public boolean tick() {
            if (++this.tick > 1200) {
                this.player.displayClientMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"), true);
                return false;
            }
            return true;
        }
    }
}
