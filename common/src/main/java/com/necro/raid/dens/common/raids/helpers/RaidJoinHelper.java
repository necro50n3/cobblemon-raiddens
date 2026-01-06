package com.necro.raid.dens.common.raids.helpers;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.ComponentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidJoinHelper extends SavedData {
    public static RaidJoinHelper INSTANCE;
    private static final Map<Player, JoinRequest> JOIN_QUEUE = new HashMap<>();

    private final Map<UUID, Participant> RAID_PARTICIPANTS = new HashMap<>();

    public static boolean isInQueue(Player player) {
        return JOIN_QUEUE.containsKey(player);
    }

    public static void addToQueue(Player player, @Nullable ItemStack key) {
        JOIN_QUEUE.put(player, new JoinRequest(player, key));
    }

    public static void removeFromQueue(Player player) {
        JoinRequest joinRequest = JOIN_QUEUE.remove(player);
        if (joinRequest == null) return;
        joinRequest.refundItem();
    }

    public static boolean addParticipant(Player player, UUID raid, boolean isHost) {
        boolean isParticipating = isParticipating(player);
        if (!isParticipating) {
            INSTANCE.RAID_PARTICIPANTS.put(player.getUUID(), new Participant(raid, isHost));
            INSTANCE.setDirty();
        }
        return isParticipating;
    }

    public static void removeParticipant(Player player) {
        INSTANCE.RAID_PARTICIPANTS.remove(player.getUUID());
        INSTANCE.setDirty();
    }

    public static void removeParticipants(Collection<Player> players) {
        players.forEach(player -> INSTANCE.RAID_PARTICIPANTS.remove(player.getUUID()));
        INSTANCE.setDirty();
    }

    public static boolean isParticipating(Player player) {
        if (JOIN_QUEUE.containsKey(player)) {
            player.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.already_in_queue"));
            return false;
        }

        Participant participant = INSTANCE.RAID_PARTICIPANTS.get(player.getUUID());
        if (participant == null) return true;

        if (participant.isHost()) player.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.already_hosting"));
        else player.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.already_participating"));
        return false;
    }

    public static void onServerClose() {
        JOIN_QUEUE.forEach((player, instance) -> instance.refundItem());
        JOIN_QUEUE.clear();
    }

    public static void serverTick() {
        JOIN_QUEUE.values().removeIf(instance -> !instance.tick());
    }

    public static void onPlayerDisconnect(Player player) {
        refundItem(player);
    }

    private static void refundItem(Player player) {
        if (!JOIN_QUEUE.containsKey(player)) return;
        JOIN_QUEUE.get(player).refundItem();
        JOIN_QUEUE.remove(player);
    }

    private record Participant(UUID raid, boolean isHost) {}

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
                this.player.sendSystemMessage(ComponentUtils.getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
                return false;
            }
            return true;
        }
    }

    public static RaidJoinHelper create() {
        return new RaidJoinHelper();
    }

    public static void initHelper(MinecraftServer server) {
        INSTANCE = server.overworld().getDataStorage().computeIfAbsent(RaidJoinHelper.type(), CobblemonRaidDens.MOD_ID);
        INSTANCE.setDirty();
    }

    public static RaidJoinHelper load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        RaidJoinHelper data = create();

        ListTag raidParticipants = compoundTag.getList("raid_participants", Tag.TAG_COMPOUND);
        for (Tag t : raidParticipants) {
            CompoundTag entry = (CompoundTag) t;
            UUID participant = entry.getUUID("participant");
            UUID raid = entry.getUUID("raid");
            boolean isHost = entry.getBoolean("is_host");

            data.RAID_PARTICIPANTS.put(participant, new Participant(raid, isHost));
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        ListTag raidParticipantsTag = new ListTag();
        for (Map.Entry<UUID, Participant> entry : RAID_PARTICIPANTS.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("participant", entry.getKey());
            e.putUUID("raid", entry.getValue().raid());
            e.putBoolean("is_host", entry.getValue().isHost());
        }
        compoundTag.put("raid_participants", raidParticipantsTag);
        return compoundTag;
    }

    @SuppressWarnings("ConstantConditions")
    public static Factory<RaidJoinHelper> type() {
        return new Factory<>(
            RaidJoinHelper::create,
            RaidJoinHelper::load,
            null
        );
    }
}
