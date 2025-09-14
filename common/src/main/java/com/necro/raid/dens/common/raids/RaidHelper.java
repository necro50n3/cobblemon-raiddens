package com.necro.raid.dens.common.raids;

import com.mojang.datafixers.util.Pair;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.dimensions.DimensionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidHelper extends SavedData {
    public static final Set<UUID> RAID_HOSTS = new HashSet<>();
    public static final Map<Player, JoinRequestInstance> JOIN_QUEUE = new HashMap<>();
    public static final Set<UUID> RAID_PARTICIPANTS = new HashSet<>();
    public static final Set<UUID> WAS_SURVIVAL = new HashSet<>();
    public static final Map<UUID, RaidInstance> ACTIVE_RAIDS = new HashMap<>();
    public static final Map<Pair<String, BlockPos>, Set<UUID>> CLEARED_RAIDS = new HashMap<>();
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

    public static void addSurvivalPlayer(Player player) {
        WAS_SURVIVAL.add(player.getUUID());
    }

    public static boolean playerWasSurvival(Player player) {
        return WAS_SURVIVAL.remove(player.getUUID());
    }

    public static boolean hasClearedRaid(Level level, BlockPos blockPos, Player player) {
        Pair<String, BlockPos> key = new Pair<>(level.dimension().location().toString(), blockPos);
        Set<UUID> cleared = RaidHelper.CLEARED_RAIDS.getOrDefault(key, new HashSet<>());
        return cleared.contains(player.getUUID());
    }

    public static void clearRaid(Level level, BlockPos blockPos, Collection<UUID> players) {
        Pair<String, BlockPos> key = new Pair<>(level.dimension().location().toString(), blockPos);
        if (!CLEARED_RAIDS.containsKey(key)) CLEARED_RAIDS.put(key, new HashSet<>());
        CLEARED_RAIDS.get(key).addAll(players);
    }

    public static void resetClearedRaids(Level level, BlockPos blockPos) {
        Pair<String, BlockPos> key = new Pair<>(level.dimension().location().toString(), blockPos);
        CLEARED_RAIDS.remove(key);
    }

    public static void resetPlayerClearedRaid(Level level, BlockPos blockPos, UUID player) {
        Pair<String, BlockPos> key = new Pair<>(level.dimension().location().toString(), blockPos);
        if (!CLEARED_RAIDS.containsKey(key)) return;
        CLEARED_RAIDS.get(key).remove(player);
    }

    public static void resetPlayerAllClearedRaids(Level level, BlockPos blockPos, UUID player) {
        Pair<String, BlockPos> key = new Pair<>(level.dimension().location().toString(), blockPos);
        if (!CLEARED_RAIDS.containsKey(key)) return;
        CLEARED_RAIDS.values().forEach(playerSet -> playerSet.remove(player));
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

    public static void onPlayerJoin(ServerPlayer player) {
        if (!WAS_SURVIVAL.contains(player.getUUID())) return;
        else if (DimensionHelper.isCustomDimension((ServerLevel) player.level())) return;
        else if (player.gameMode.getGameModeForPlayer() != GameType.ADVENTURE) return;
        player.setGameMode(GameType.SURVIVAL);
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

    public static String acceptRaidCommand(Player player, RaidCrystalBlockEntity blockEntity, BlockPos blockPos) {
        return String.format("/crd_raids acceptrequest %s %s %s %s %s",
            player.getName().getString(),
            blockEntity.getLevel().dimension().location(),
            blockPos.getX(), blockPos.getY(), blockPos.getZ()
        );
    }

    public static String rejectRaidCommand(Player player) {
        return String.format("/crd_raids denyrequest %s", player.getName().getString());
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
            compoundTag.getList("raid_hosts", Tag.TAG_STRING).forEach(host -> RAID_HOSTS.add(UUID.fromString(host.getAsString())));
        }
        if (compoundTag.contains("raid_participants")) {
            compoundTag.getList("raid_participants", Tag.TAG_STRING).forEach(p -> RAID_PARTICIPANTS.add(UUID.fromString(p.getAsString())));
        }
        if (compoundTag.contains("was_survival")) {
            compoundTag.getList("was_survival", Tag.TAG_STRING).forEach(s -> WAS_SURVIVAL.add(UUID.fromString(s.getAsString())));
        }

        ListTag clearedRaids = compoundTag.getList("cleared_raids", Tag.TAG_COMPOUND);
        for (Tag t : clearedRaids) {
            CompoundTag entry = (CompoundTag) t;
            String dimension = entry.getString("dimension");
            if (dimension.isEmpty()) dimension = "minecraft:overworld";
            Optional<BlockPos> pos = NbtUtils.readBlockPos(entry, "pos");
            if (pos.isEmpty()) continue;

            Set<UUID> players = new HashSet<>();
            ListTag uuidList = entry.getList("players", Tag.TAG_INT_ARRAY);
            for (Tag uuidTag : uuidList) {
                players.add(NbtUtils.loadUUID(uuidTag));
            }
            CLEARED_RAIDS.put(new Pair<>(dimension, pos.get()), players);
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

        ListTag wasSurvivalTag = new ListTag();
        WAS_SURVIVAL.forEach(uuid -> wasSurvivalTag.add(StringTag.valueOf(uuid.toString())));
        compoundTag.put("was_survival", wasSurvivalTag);

        ListTag clearedRaidsTag = new ListTag();
        for (Map.Entry<Pair<String, BlockPos>, Set<UUID>> entry : CLEARED_RAIDS.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("dimension", entry.getKey().getFirst());
            e.put("pos", NbtUtils.writeBlockPos(entry.getKey().getSecond()));

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
