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
    private static final Map<String, List<Integer>> INDEX_CACHE = new HashMap<>();

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

    public static ResourceLocation getRandomRaidBoss(RandomSource random, RaidTier tier, RaidType type, RaidFeature feature) {
        BitSet result = (BitSet) RAIDS_BY_TIER.get(tier).clone();
        if (type != null) result.and(RAIDS_BY_TYPE.get(type));
        if (feature != null) result.and(RAIDS_BY_FEATURE.get(feature));

        List<Integer> matches = new ArrayList<>();
        for (int i = result.nextSetBit(0); i >= 0; i = result.nextSetBit(++i)) matches.add(i);
        if (matches.isEmpty()) return null;

        String key = tier + ":" + type + ":" + feature;
        float[] cachedWeights;
        List<Integer> cachedIndexes;
        if (WEIGHTS_CACHE.containsKey(key)) {
            cachedWeights = WEIGHTS_CACHE.get(key);
            cachedIndexes = INDEX_CACHE.get(key);
        } else {
            cachedWeights = new float[matches.size()];
            float sum = 0f;
            for (int i = 0; i < matches.size(); i++) {
                sum += (float) RAID_LOOKUP.get(RAID_LIST.get(matches.get(i))).getWeight();
                cachedWeights[i] = sum;
            }
            cachedIndexes = matches;
            WEIGHTS_CACHE.put(key, cachedWeights);
            INDEX_CACHE.put(key, cachedIndexes);
        }

        float roll = random.nextFloat() * cachedWeights[cachedWeights.length - 1];
        int idx = Arrays.binarySearch(cachedWeights, roll);
        if (idx < 0) idx = -idx - 1;

        return RAID_LIST.get(cachedIndexes.get(idx));
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
