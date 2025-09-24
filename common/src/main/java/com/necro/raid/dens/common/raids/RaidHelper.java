package com.necro.raid.dens.common.raids;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidHelper extends SavedData {
    public static RaidHelper INSTANCE;

    public static final Map<Player, JoinRequestInstance> JOIN_QUEUE = new HashMap<>();
    public static final Map<UUID, RaidInstance> ACTIVE_RAIDS = new HashMap<>();
    public static final Map<Player, RequestHandler> REQUEST_QUEUE = new HashMap<>();
    public static final Map<Player, RewardHandler> REWARD_QUEUE = new HashMap<>();

    public final Set<UUID> RAID_HOSTS = new HashSet<>();
    public final Set<UUID> RAID_PARTICIPANTS = new HashSet<>();
    public final Map<UUID, Set<UUID>> CLEARED_RAIDS = new HashMap<>();

    public static boolean addToQueue(Player player, @Nullable ItemStack key) {
        if (JOIN_QUEUE.containsKey(player)) return false;
        JOIN_QUEUE.put(player, new JoinRequestInstance(player, key));
        return true;
    }

    public static void addRequest(ServerPlayer host, Player player, RaidCrystalBlockEntity blockEntity) {
        if (!REQUEST_QUEUE.containsKey(host)) REQUEST_QUEUE.put(host, new RequestHandler(blockEntity));
        RequestHandler handler = REQUEST_QUEUE.get(host);
        handler.addPlayer(player);
    }

    public static RequestHandler getRequest(ServerPlayer host) {
        if (!REQUEST_QUEUE.containsKey(host)) return null;
        return REQUEST_QUEUE.get(host);
    }

    public static void addHost(Player player) {
        INSTANCE.RAID_HOSTS.add(player.getUUID());
        INSTANCE.setDirty();
    }

    public static void addParticipant(Player player) {
        INSTANCE.RAID_PARTICIPANTS.add(player.getUUID());
        INSTANCE.setDirty();
    }

    public static boolean hasClearedRaid(UUID uuid, Player player) {
        Set<UUID> cleared = INSTANCE.CLEARED_RAIDS.getOrDefault(uuid, new HashSet<>());
        return cleared.contains(player.getUUID());
    }

    public static void clearRaid(UUID uuid, Collection<UUID> players) {
        if (!INSTANCE.CLEARED_RAIDS.containsKey(uuid)) INSTANCE.CLEARED_RAIDS.put(uuid, new HashSet<>());
        INSTANCE.CLEARED_RAIDS.get(uuid).addAll(players);
        INSTANCE.setDirty();
    }

    public static void resetClearedRaids(UUID uuid) {
        INSTANCE.CLEARED_RAIDS.remove(uuid);
        INSTANCE.setDirty();
    }

    public static void resetPlayerClearedRaid(UUID uuid, UUID player) {
        if (!INSTANCE.CLEARED_RAIDS.containsKey(uuid)) return;
        INSTANCE.CLEARED_RAIDS.get(uuid).remove(player);
        INSTANCE.setDirty();
    }

    public static void resetPlayerAllClearedRaids(UUID player) {
        INSTANCE.CLEARED_RAIDS.values().forEach(playerSet -> playerSet.remove(player));
        INSTANCE.setDirty();
    }

    public static boolean isAlreadyHosting(Player player) {
        return isAlreadyHosting(player.getUUID());
    }

    public static boolean isAlreadyHosting(UUID player) {
        return INSTANCE.RAID_HOSTS.contains(player);
    }

    public static boolean isAlreadyParticipating(Player player) {
        return isAlreadyParticipating(player.getUUID());
    }

    public static boolean isAlreadyParticipating(UUID player) {
        return INSTANCE.RAID_PARTICIPANTS.contains(player);
    }

    public static void removeHost(UUID player) {
        INSTANCE.RAID_HOSTS.remove(player);
        INSTANCE.setDirty();
    }

    public static void removeParticipant(UUID player) {
        INSTANCE.RAID_PARTICIPANTS.remove(player);
        INSTANCE.setDirty();
    }

    public static void finishRaid(Set<UUID> players) {
        INSTANCE.RAID_PARTICIPANTS.removeAll(players);
        INSTANCE.setDirty();
    }

    public static void onPlayerDisconnect(Player player) {
        if (!JOIN_QUEUE.containsKey(player)) return;
        JOIN_QUEUE.get(player).refundItem();
        JOIN_QUEUE.remove(player);
    }

    public static void onServerClose() {
        JOIN_QUEUE.forEach((player, instance) -> instance.refundItem());
        JOIN_QUEUE.clear();
    }

    public static void serverTick() {
        JOIN_QUEUE.values().removeIf(instance -> !instance.tick());
    }

    public static void commonTick() {
        List<RaidInstance> raids = new ArrayList<>(ACTIVE_RAIDS.values());
        raids.forEach(RaidInstance::tick);
    }

    public static Component getSystemMessage(String translatable) {
        return getSystemMessage(Component.translatable(translatable));
    }

    public static Component getSystemMessage(MutableComponent component) {
        return component.withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
    }

    public static class JoinRequestInstance {
        private final Player player;
        private final ItemStack itemStack;
        private int tick;

        public JoinRequestInstance(Player player, @Nullable ItemStack itemStack) {
            this.player = player;
            this.itemStack = itemStack;
            this.tick = 0;
        }

        public void refundItem() {
            if (this.itemStack != null) player.addItem(this.itemStack);
        }

        public boolean tick() {
            if (++this.tick > 1200) {
                this.player.sendSystemMessage(getSystemMessage("message.cobblemonraiddens.raid.request_time_out"));
                return false;
            }
            return true;
        }
    }

    public static RaidHelper create() {
        return new RaidHelper();
    }

    public static RaidHelper load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        RaidHelper data = create();

        if (compoundTag.contains("raid_hosts")) {
            compoundTag.getList("raid_hosts", Tag.TAG_STRING).forEach(host -> data.RAID_HOSTS.add(UUID.fromString(host.getAsString())));
        }
        if (compoundTag.contains("raid_participants")) {
            compoundTag.getList("raid_participants", Tag.TAG_STRING).forEach(p -> data.RAID_PARTICIPANTS.add(UUID.fromString(p.getAsString())));
        }

        ListTag clearedRaids = compoundTag.getList("cleared_raids", Tag.TAG_COMPOUND);
        for (Tag t : clearedRaids) {
            CompoundTag entry = (CompoundTag) t;
            String uuid = entry.getString("uuid");
            if (uuid.isEmpty()) continue;

            Set<UUID> players = new HashSet<>();
            ListTag uuidList = entry.getList("players", Tag.TAG_INT_ARRAY);
            for (Tag uuidTag : uuidList) {
                players.add(NbtUtils.loadUUID(uuidTag));
            }
            data.CLEARED_RAIDS.put(UUID.fromString(uuid), players);
        }

        return data;
    }

    public static void initHelper(MinecraftServer server) {
        INSTANCE = server.overworld().getDataStorage().computeIfAbsent(RaidHelper.type(), CobblemonRaidDens.MOD_ID);
        INSTANCE.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag raidHostsTag = new ListTag();
        RAID_HOSTS.forEach(uuid -> raidHostsTag.add(StringTag.valueOf(uuid.toString())));
        compoundTag.put("raid_hosts", raidHostsTag);

        ListTag raidParticipantsTag = new ListTag();
        RAID_PARTICIPANTS.forEach(uuid -> raidParticipantsTag.add(StringTag.valueOf(uuid.toString())));
        compoundTag.put("raid_participants", raidParticipantsTag);

        ListTag clearedRaidsTag = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : CLEARED_RAIDS.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("uuid", entry.getKey().toString());

            ListTag uuidList = new ListTag();
            for (UUID uuid : entry.getValue()) {
                uuidList.add(NbtUtils.createUUID(uuid));
            }
            e.put("players", uuidList);

            clearedRaidsTag.add(e);
        }
        compoundTag.put("cleared_raids", clearedRaidsTag);

        return compoundTag;
    }

    public static Factory<RaidHelper> type() {
        return new Factory<>(
            RaidHelper::create,
            RaidHelper::load,
            null
        );
    }
}
