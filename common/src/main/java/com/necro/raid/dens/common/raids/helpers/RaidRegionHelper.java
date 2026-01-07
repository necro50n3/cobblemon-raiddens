package com.necro.raid.dens.common.raids.helpers;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

public class RaidRegionHelper extends SavedData {
    public static RaidRegionHelper INSTANCE;
    private static final int SPACING = 2000;
    private static final int RAID_CAP = 10000;

    private final Map<UUID, Integer> INDEX_MAP = new HashMap<>();
    private final Map<Integer, RaidRegion> REGION_MAP = new HashMap<>();

    public static boolean createRegion(UUID raid, ResourceLocation structure, BlockPos homePos, ServerLevel homeLevel) {
        OptionalInt optionalIndex = IntStream.range(0, RAID_CAP)
            .filter(i -> !INSTANCE.REGION_MAP.containsKey(i))
            .findFirst();
        if (optionalIndex.isEmpty()) return false;

        int index = optionalIndex.getAsInt();
        RaidRegion region = new RaidRegion(coordFromIndex(index), structure, homePos, homeLevel.dimension().location());
        INSTANCE.INDEX_MAP.put(raid, index);
        INSTANCE.REGION_MAP.put(index, region);
        INSTANCE.setDirty();
        return true;
    }

    public static RaidRegion getRegion(UUID raid) {
        Integer index = INSTANCE.INDEX_MAP.get(raid);
        if (index == null) return null;
        return INSTANCE.REGION_MAP.get(index);
    }

    public static void clearRegion(UUID raid, ServerLevel level) {
        Integer index = INSTANCE.INDEX_MAP.remove(raid);
        if (index == null) return;
        INSTANCE.REGION_MAP.remove(index).clearRegion(level);
        INSTANCE.setDirty();
    }

    private static BlockPos coordFromIndex(int index) {
        if (index == 0) return BlockPos.ZERO;

        int k = (int) Math.ceil((Math.sqrt(index + 1) - 1) / 2);
        int side = 2 * k;
        int start = (2 * k - 1) * (2 * k - 1);
        int t = index - start;

        int x;
        int z;

        if (t < side) {
            x = k;
            z = -k + t;
        }
        else if (t < 2 * side) {
            x = k - (t - side);
            z = k;
        }
        else if (t < 3 * side) {
            x = -k;
            z = k - (t - 2 * side);
        }
        else {
            x = -k + (t - 3 * side);
            z = -k;
        }

        return new BlockPos(x * SPACING, 0, z * SPACING);
    }

    public static RaidRegionHelper create() {
        return new RaidRegionHelper();
    }

    public static void initHelper(MinecraftServer server) {
        INSTANCE = server.overworld().getDataStorage().computeIfAbsent(RaidRegionHelper.type(), CobblemonRaidDens.MOD_ID);
        INSTANCE.setDirty();
    }

    public static RaidRegionHelper load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        RaidRegionHelper data = create();

        ListTag index = compoundTag.getList("raid_index_map", Tag.TAG_COMPOUND);
        for (Tag t : index) {
            CompoundTag entry = (CompoundTag) t;
            UUID raid = entry.getUUID("raid");
            int indexValue = entry.getInt("index");
            data.INDEX_MAP.put(raid, indexValue);
        }

        ListTag region = compoundTag.getList("raid_index_map", Tag.TAG_COMPOUND);
        for (Tag t : region) {
            CompoundTag entry = (CompoundTag) t;
            int indexValue = entry.getInt("index");
            RaidRegion regionValue = RaidRegion.load(entry.getCompound("region"), provider);
            data.REGION_MAP.put(indexValue, regionValue);
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        ListTag index = new ListTag();
        for (Map.Entry<UUID, Integer> entry : INDEX_MAP.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putUUID("raid", entry.getKey());
            e.putInt("index", entry.getValue());
        }
        compoundTag.put("raid_index_map", index);

        ListTag region = new ListTag();
        for (Map.Entry<Integer, RaidRegion> entry : REGION_MAP.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putInt("index", entry.getKey());
            e.put("region", entry.getValue().save(new CompoundTag()));
        }
        compoundTag.put("raid_region_map", region);
        return compoundTag;
    }

    @SuppressWarnings("ConstantConditions")
    public static Factory<RaidRegionHelper> type() {
        return new Factory<>(
            RaidRegionHelper::create,
            RaidRegionHelper::load,
            null
        );
    }
}
