package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.*;

public class RaidRegistry {
    public static final ResourceKey<Registry<RaidBoss>> RAID_BOSS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("raid", "boss"));
    private static final Map<RaidTier, Set<RaidBoss>> RAID_BOSS_COLLECTION = new HashMap<>();
    private static final Map<RaidTier, DoubleWeightedRandomMap<RaidBoss>> RAID_BOSSES = new HashMap<>();

    static {
        for (RaidTier tier : RaidTier.values()) {
            RAID_BOSS_COLLECTION.put(tier, new HashSet<>());
        }
    }

    public static void register(RaidBoss raidBoss) {
        if (raidBoss.getProperties().getSpecies() == null) return;
        RAID_BOSS_COLLECTION.get(raidBoss.getTier()).add(raidBoss);
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
        DoubleWeightedRandomMap<RaidBoss> map = new DoubleWeightedRandomMap<>();
        raidBosses.forEach(raidBoss -> map.add(raidBoss, raidBoss.getWeight()));
        RAID_BOSSES.put(tier, map);
    }

    public static RaidBoss getRandomRaidBoss(RandomSource random, RaidTier tier) {
        Optional<RaidBoss> raidBoss = RaidRegistry.RAID_BOSSES.get(tier).getRandom(random);
        return raidBoss.orElse(null);
    }
}
