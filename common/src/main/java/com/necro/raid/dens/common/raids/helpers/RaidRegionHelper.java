package com.necro.raid.dens.common.raids.helpers;

import com.necro.raid.dens.common.data.dimension.RaidRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

public class RaidRegionHelper {
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
        RaidRegion region = new RaidRegion(coordFromIndex(index), structure, homePos, homeLevel);
        INSTANCE.INDEX_MAP.put(raid, index);
        INSTANCE.REGION_MAP.put(index, region);
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
}
