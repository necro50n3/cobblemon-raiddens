package com.necro.raid.dens.common.raids.helpers;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.dimension.RaidAllocation;
import com.necro.raid.dens.common.data.dimension.RaidRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

public class RaidRegionHelper {
    private static final int SPACING = 2000;

    private static final Map<UUID, Integer> INDEX_MAP = new HashMap<>();
    private static final Map<Integer, RaidRegion> REGION_MAP = new HashMap<>();

    public static RaidRegion createRegion(UUID raid, ResourceLocation structure, RandomSource random) {
        int index = CobblemonRaidDens.CONFIG.raid_allocation == RaidAllocation.SPIRAL ? getNextIndex() : getRandomIndex(random);
        if (index == -1) return null;

        RaidRegion region = new RaidRegion(coordFromIndex(index), structure);
        INDEX_MAP.put(raid, index);
        REGION_MAP.put(index, region);
        return region;
    }

    private static int getNextIndex() {
        OptionalInt optionalIndex = IntStream.range(0, CobblemonRaidDens.CONFIG.raid_cap)
            .filter(i -> !REGION_MAP.containsKey(i))
            .findFirst();
        if (optionalIndex.isEmpty()) return -1;
        return optionalIndex.getAsInt();
    }

    private static int getRandomIndex(RandomSource random) {
        int[] indices = IntStream.range(0, CobblemonRaidDens.CONFIG.raid_cap)
            .filter(i -> !REGION_MAP.containsKey(i))
            .toArray();
        if (indices.length == 0) return -1;
        return indices[random.nextInt(Math.min(indices.length, 9))];
    }

    public static RaidRegion getRegion(UUID raid) {
        Integer index = INDEX_MAP.get(raid);
        if (index == null) return null;
        return REGION_MAP.get(index);
    }

    public static void clearRegion(UUID raid, ServerLevel level) {
        Integer index = INDEX_MAP.remove(raid);
        if (index == null) return;
        REGION_MAP.remove(index).clearRegion(level);
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
