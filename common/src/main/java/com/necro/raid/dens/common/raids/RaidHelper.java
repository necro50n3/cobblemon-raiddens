package com.necro.raid.dens.common.raids;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidHelper extends SavedData {
    public static final Set<UUID> RAID_HOSTS = new HashSet<>();
    public static final Map<Player, JoinRequestInstance> JOIN_QUEUE = new HashMap<>();
    public static final Set<UUID> RAID_PARTICIPANTS = new HashSet<>();
    public static final Map<UUID, RaidInstance> ACTIVE_RAIDS = new HashMap<>();
    public static final Map<BlockPos, Set<UUID>> CLEARED_RAIDS = new HashMap<>();
    public static final Map<Player, RewardHandler> REWARD_QUEUE = new HashMap<>();

    public static boolean addToQueue(Player player, @Nullable ItemStack key) {
        if (JOIN_QUEUE.containsKey(player)) return false;
        JOIN_QUEUE.put(player, new JoinRequestInstance(player, key));
        return true;
    }

    public static void addHost(Player player) {
        RAID_HOSTS.add(player.getUUID());
    }

    public static void addParticipant(Player player) {
        RAID_PARTICIPANTS.add(player.getUUID());
    }

    public static void clearRaid(BlockPos blockPos, Collection<UUID> players) {
        if (!CLEARED_RAIDS.containsKey(blockPos)) CLEARED_RAIDS.put(blockPos, new HashSet<>());
        CLEARED_RAIDS.get(blockPos).addAll(players);
    }

    public static void resetClearedRaids(BlockPos blockPos) {
        if (!CLEARED_RAIDS.containsKey(blockPos)) return;
        CLEARED_RAIDS.get(blockPos).clear();
    }

    public static boolean isAlreadyHosting(Player player) {
        return RAID_HOSTS.contains(player.getUUID());
    }

    public static boolean isAlreadyParticipating(Player player) {
        return RAID_PARTICIPANTS.contains(player.getUUID());
    }

    public static void removeHost(UUID player) {
        RAID_HOSTS.remove(player);
    }

    public static void removeParticipant(UUID player) {
        RAID_PARTICIPANTS.remove(player);
    }

    public static void finishRaid(Set<UUID> players) {
        for (UUID player : players) {
            removeParticipant(player);
        }
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
        Collection<JoinRequestInstance> instances = JOIN_QUEUE.values();
        instances.forEach(instance -> {
            if (!instance.tick()) instances.remove(instance);
        });

    }

    public static void commonTick() {
        List<RaidInstance> raids = new ArrayList<>(ACTIVE_RAIDS.values());
        raids.forEach(RaidInstance::tick);
    }

    public static String acceptRaidCommand(Player host, Player player, RaidCrystalBlockEntity blockEntity, BlockPos blockPos) {
        return String.format("/crd_raids acceptrequest %s %s %s %s %s",
            player.getName().getString(),
            blockEntity.getDimension().dimension().location(),
            blockPos.getX(), blockPos.getY(), blockPos.getZ()
        );
    }

    public static String rejectRaidCommand(Player host, Player player) {
        return String.format("/crd_raids acceptrequest %s", player.getName().getString());
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
            ((ListTag) compoundTag.get("raid_hosts")).forEach(host -> RAID_HOSTS.add(UUID.fromString(host.getAsString())));
        }
        if (compoundTag.contains("raid_participants")) {
            ((ListTag) compoundTag.get("raid_participants")).forEach(host -> RAID_PARTICIPANTS.add(UUID.fromString(host.getAsString())));
        }

        ListTag clearedRaids = compoundTag.getList("cleared_raids", Tag.TAG_COMPOUND);
        for (Tag t : clearedRaids) {
            CompoundTag entry = (CompoundTag) t;
            Optional<BlockPos> pos = NbtUtils.readBlockPos(entry, "pos");
            if (pos.isEmpty()) continue;

            Set<UUID> players = new HashSet<>();
            ListTag uuidList = entry.getList("players", Tag.TAG_INT_ARRAY);
            for (Tag uuidTag : uuidList) {
                players.add(NbtUtils.loadUUID(uuidTag));
            }
            CLEARED_RAIDS.put(pos.get(), players);
        }

        return data;
    }

    public static void initHelper(MinecraftServer server) {
        SavedData data = server.overworld().getDataStorage().computeIfAbsent(RaidHelper.type(), CobblemonRaidDens.MOD_ID);
        data.setDirty();
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
        for (Map.Entry<BlockPos, Set<UUID>> entry : CLEARED_RAIDS.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.put("pos", NbtUtils.writeBlockPos(entry.getKey()));

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
