package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.*;

public class RaidRegistry {
    public static final ResourceKey<Registry<RaidBoss>> RAID_BOSS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("raid", "boss"));
    public static Registry<RaidBoss> REGISTRY;

    private static final Map<RaidTier, BitSet> RAIDS_BY_TIER = new EnumMap<>(RaidTier.class);
    private static final Map<RaidType, BitSet> RAIDS_BY_TYPE = new EnumMap<>(RaidType.class);
    private static final Map<RaidFeature, BitSet> RAIDS_BY_FEATURE = new EnumMap<>(RaidFeature.class);
    private static final List<ResourceLocation> RAID_LIST = new ArrayList<>();
    private static final Map<ResourceLocation, RaidBoss> RAID_LOOKUP = new HashMap<>();

    private static final Map<String, float[]> WEIGHTS_CACHE = new HashMap<>();
    private static final Map<String, int[]> INDEX_CACHE = new HashMap<>();

    public static void register(RaidBoss raidBoss) {
        if (raidBoss.getProperties().getSpecies() == null) return;

        int index = RAID_LIST.size();
        RAID_LIST.add(raidBoss.getId());
        RAID_LOOKUP.put(raidBoss.getId(), raidBoss);

        RAIDS_BY_TIER.computeIfAbsent(raidBoss.getTier(), tier -> new BitSet()).set(index);
        RAIDS_BY_TYPE.computeIfAbsent(raidBoss.getType(), type -> new BitSet()).set(index);
        RAIDS_BY_FEATURE.computeIfAbsent(raidBoss.getFeature(), feature -> new BitSet()).set(index);

        raidBoss.getTier().setPresent();
        raidBoss.getType().setPresent();
    }

    public static void populateWeightedList() {}

    public static RaidBoss getRaidBoss(ResourceLocation location) {
        return RAID_LOOKUP.get(location);
    }

    public static boolean exists(ResourceLocation location) {
        return RAID_LOOKUP.containsKey(location);
    }

    private static float[] buildWeights(int[] matches) {
        float[] weights = new float[matches.length];
        float sum = 0f;
        for (int i = 0; i < matches.length; i++) {
            sum += (float) RAID_LOOKUP.get(RAID_LIST.get(matches[i])).getWeight();
            weights[i] = sum;
        }
        return weights;
    }

    private static ResourceLocation roll(RandomSource random, float[] weights, int[] indexes) {
        float roll = random.nextFloat() * weights[weights.length - 1];
        int idx = Arrays.binarySearch(weights, roll);
        if (idx < 0) idx = -idx - 1;

        return RAID_LIST.get(indexes[idx]);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, List<RaidTier> tiers, List<RaidType> types, List<RaidFeature> features) {
        if (tiers == null || tiers.isEmpty()) return null;

        boolean cacheable = (tiers.size() == 1 && (types == null || types.size() <= 1) && (features == null || features.isEmpty()));
        String key = tiers.getFirst() + ":" + (types == null ? null : types.getFirst());
        if (cacheable && WEIGHTS_CACHE.containsKey(key)) return roll(random, WEIGHTS_CACHE.get(key), INDEX_CACHE.get(key));

        BitSet result = new BitSet();

        for (RaidTier tier : tiers) {
            BitSet set = RAIDS_BY_TIER.get(tier);
            if (set != null) result.or(set);
        }

        if (types != null && !types.isEmpty()) {
            BitSet typeSet = new BitSet();
            for (RaidType type : types) {
                BitSet set = RAIDS_BY_TYPE.get(type);
                if (set != null) typeSet.or(set);
            }
            result.and(typeSet);
        }

        if (features != null && !features.isEmpty()) {
            BitSet featureSet = new BitSet();
            for (RaidFeature feature : features) {
                BitSet set = RAIDS_BY_FEATURE.get(feature);
                if (set != null) featureSet.or(set);
            }
            result.and(featureSet);
        }

        int size = result.cardinality();
        if (size == 0) return null;
        int[] matches = new int[size];
        for (int i = result.nextSetBit(0), idx = 0; i >= 0; i = result.nextSetBit(i + 1), idx++) {
            matches[idx] = i;
        }

        float[] cachedWeights = buildWeights(matches);

        if (cacheable) {
            WEIGHTS_CACHE.put(key, cachedWeights);
            INDEX_CACHE.put(key, matches);
        }

        return roll(random, cachedWeights, matches);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, RaidTier tier, RaidType type, RaidFeature feature) {
        return getRandomRaidBoss(random, List.of(tier), type == null ? null : List.of(type), feature == null ? null : List.of(feature));
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, RaidTier tier) {
        return getRandomRaidBoss(random, tier, null, null);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level, RaidType type, RaidFeature feature) {
        return getRandomRaidBoss(random, RaidTier.getWeightedRandom(random, level), type, feature);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level) {
        return getRandomRaidBoss(random, level, null, null);
    }

    public static void clear() {
        RAIDS_BY_TIER.values().forEach(BitSet::clear);
        RAIDS_BY_TYPE.values().forEach(BitSet::clear);
        RAIDS_BY_FEATURE.values().forEach(BitSet::clear);
        RAID_LIST.clear();
        RAID_LOOKUP.clear();
        WEIGHTS_CACHE.clear();
        INDEX_CACHE.clear();

        for (RaidTier tier : RaidTier.values()) { tier.setPresent(false); }
        for (RaidType type : RaidType.values()) { type.setPresent(false); }
    }

    public static void initRaidBosses(MinecraftServer server) {
        REGISTRY = server.registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY);
    }
}
