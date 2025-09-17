package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidBoss;
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

    private static final Map<RaidTier, Set<RaidBoss>> RAID_BOSS_COLLECTION = new HashMap<>();
    private static final Map<RaidTier, DoubleWeightedRandomMap<ResourceLocation>> RAID_BOSSES = new HashMap<>();
    private static final Map<ResourceLocation, RaidBoss> RAID_BOSS_MAP = new HashMap<>();

    static {
        for (RaidTier tier : RaidTier.values()) {
            RAID_BOSS_COLLECTION.put(tier, new HashSet<>());
        }
    }

    public static void register(RaidBoss raidBoss) {
        if (raidBoss.getProperties().getSpecies() == null) return;
        RAID_BOSS_COLLECTION.get(raidBoss.getTier()).add(raidBoss);
        RAID_BOSS_MAP.put(raidBoss.getId(), raidBoss);
        raidBoss.getTier().setPresent();
        raidBoss.getType().setPresent();
    }

    public static void populateWeightedList() {
        for (RaidTier tier : RaidTier.values()) {
            addWeightedList(tier, RAID_BOSS_COLLECTION.get(tier));
            RAID_BOSS_COLLECTION.get(tier).clear();
        }
    }

    private static void addWeightedList(RaidTier tier, Set<RaidBoss> raidBosses) {
        DoubleWeightedRandomMap<ResourceLocation> map = new DoubleWeightedRandomMap<>();
        raidBosses.forEach(raidBoss -> map.add(raidBoss.getId(), raidBoss.getWeight()));
        RAID_BOSSES.put(tier, map);
    }

    public static RaidBoss getRaidBoss(ResourceLocation location) {
        return RAID_BOSS_MAP.get(location);
    }

    public static RaidBoss getRandomRaidBoss(RandomSource random, RaidTier tier) {
        ResourceLocation location = getRandomRaidBossResource(random, tier);
        return location == null ? null : getRaidBoss(location);
    }

    public static RaidBoss getRandomRaidBoss(RandomSource random, Level level) {
        return getRandomRaidBoss(random, RaidTier.getWeightedRandom(random, level));
    }

    public static ResourceLocation getRandomRaidBossResource(RandomSource random, RaidTier tier) {
        Optional<ResourceLocation> location = RaidRegistry.RAID_BOSSES.get(tier).getRandom(random);
        return location.orElse(null);
    }

    public static ResourceLocation getRandomRaidBossResource(RandomSource random, Level level) {
        return getRandomRaidBossResource(random, RaidTier.getWeightedRandom(random, level));
    }

    public static void clear() {
        RAID_BOSS_MAP.clear();
        for (RaidTier tier : RaidTier.values()) { tier.setPresent(false); }
        for (RaidType type : RaidType.values()) { type.setPresent(false); }
    }

    public static void initRaidBosses(MinecraftServer server) {
        REGISTRY = server.registryAccess().registryOrThrow(RaidRegistry.RAID_BOSS_KEY);
    }
}
