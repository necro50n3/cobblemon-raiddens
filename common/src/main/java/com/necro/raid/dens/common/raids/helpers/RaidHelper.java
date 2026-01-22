package com.necro.raid.dens.common.raids.helpers;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.RaidState;
import com.necro.raid.dens.common.raids.RequestHandler;
import com.necro.raid.dens.common.raids.RewardHandler;
import com.necro.raid.dens.common.util.IRaidTeleporter;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaidHelper extends SavedData {
    public static RaidHelper INSTANCE;

    public static final Map<UUID, RaidInstance> ACTIVE_RAIDS = new HashMap<>();
    public static final Map<UUID, RequestHandler> REQUEST_QUEUE = new HashMap<>();
    public static final Map<UUID, RewardHandler> REWARD_QUEUE = new HashMap<>();

    private final Map<UUID, Set<UUID>> CLEARED_RAIDS = new HashMap<>();
    private final Map<UUID, RaidState> RAID_CLOSE_QUEUE = new HashMap<>();

    public static void initRequest(ServerPlayer host, RaidCrystalBlockEntity blockEntity) {
        if (REQUEST_QUEUE.containsKey(host.getUUID())) return;
        REQUEST_QUEUE.put(host.getUUID(), new RequestHandler(blockEntity));
    }

    public static void addRequest(ServerPlayer host, Player player) {
        UUID uuid = host.getUUID();
        if (!REQUEST_QUEUE.containsKey(uuid)) return;
        REQUEST_QUEUE.get(uuid).addPlayer(player);
    }

    public static RequestHandler getRequest(ServerPlayer host) {
        if (!REQUEST_QUEUE.containsKey(host.getUUID())) return null;
        return REQUEST_QUEUE.get(host.getUUID());
    }

    public static void removeRequests(UUID host) {
        REQUEST_QUEUE.remove(host);
    }

    public static boolean hasClearedRaid(UUID raid, Player player) {
        Set<UUID> cleared = INSTANCE.CLEARED_RAIDS.getOrDefault(raid, new HashSet<>());
        return cleared.contains(player.getUUID());
    }

    public static void closeRaid(UUID raid, RaidState raidState, ServerLevel level) {
        RaidInstance instance = ACTIVE_RAIDS.remove(raid);
        if (instance == null) return;

        if (raidState == RaidState.CANCELLED) RaidRegionHelper.clearRegion(raid, level);
        else INSTANCE.RAID_CLOSE_QUEUE.put(raid, raidState);
    }

    public static void clearRaid(UUID raid, Collection<? extends Player> players) {
        if (!INSTANCE.CLEARED_RAIDS.containsKey(raid)) INSTANCE.CLEARED_RAIDS.put(raid, new HashSet<>());
        for (Player player : players) INSTANCE.CLEARED_RAIDS.get(raid).add(player.getUUID());
        INSTANCE.setDirty();
    }

    public static void resetClearedRaids(UUID raid) {
        if (INSTANCE == null) return;
        INSTANCE.CLEARED_RAIDS.remove(raid);
        INSTANCE.setDirty();
    }

    public static void resetPlayerClearedRaid(UUID raid, UUID player) {
        if (!INSTANCE.CLEARED_RAIDS.containsKey(raid)) return;
        INSTANCE.CLEARED_RAIDS.get(raid).remove(player);
        INSTANCE.setDirty();
    }

    public static void resetPlayerAllClearedRaids(UUID player) {
        INSTANCE.CLEARED_RAIDS.values().forEach(playerSet -> playerSet.remove(player));
        INSTANCE.setDirty();
    }

    public static boolean hasRaidState(UUID raid) {
        return INSTANCE.RAID_CLOSE_QUEUE.containsKey(raid);
    }

    public static RaidState getRaidState(UUID raid) {
        RaidState state = INSTANCE.RAID_CLOSE_QUEUE.remove(raid);
        INSTANCE.setDirty();
        return state;
    }

    public static void teleportFromRaid(ServerPlayer player) {
        if (RaidUtils.isRaidDimension(player.level())) ((IRaidTeleporter) player).crd_returnHome();
    }

    public static void onServerClose(MinecraftServer server) {
        server.execute(RaidHelper::closeAllRaids);
    }

    private static void closeAllRaids() {
        REQUEST_QUEUE.forEach((uuid, handler) -> handler.getBlockEntity().closeRaid());
    }

    public static void commonTick() {
        List<RaidInstance> raids = new ArrayList<>(ACTIVE_RAIDS.values());
        raids.forEach(RaidInstance::tick);
    }

    public static RaidHelper create() {
        return new RaidHelper();
    }

    public static void initHelper(MinecraftServer server) {
        INSTANCE = server.overworld().getDataStorage().computeIfAbsent(RaidHelper.type(),  CobblemonRaidDens.MOD_ID);
        INSTANCE.setDirty();
    }

    public static RaidHelper load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        RaidHelper data = create();

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

        ListTag raidCloseQueue = compoundTag.getList("raid_close_queue", Tag.TAG_COMPOUND);
        for (Tag t : raidCloseQueue) {
            CompoundTag entry = (CompoundTag) t;
            String uuid = entry.getString("uuid");
            if (uuid.isEmpty()) continue;
            RaidState state = RaidState.fromString(entry.getString("state"));

            data.RAID_CLOSE_QUEUE.put(UUID.fromString(uuid), state);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
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

        ListTag raidCloseQueue = new ListTag();
        for (Map.Entry<UUID, RaidState> entry : RAID_CLOSE_QUEUE.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putString("uuid", entry.getKey().toString());
            e.putString("state", entry.getValue().getSerializedName());

            raidCloseQueue.add(e);
        }
        compoundTag.put("raid_close_queue", raidCloseQueue);

        return compoundTag;
    }

    @SuppressWarnings("ConstantConditions")
    public static Factory<RaidHelper> type() {
        return new Factory<>(
            RaidHelper::create,
            RaidHelper::load,
            null
        );
    }
}
