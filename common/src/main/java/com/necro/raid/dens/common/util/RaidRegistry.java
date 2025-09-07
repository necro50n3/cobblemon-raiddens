package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaidRegistry {
    public static final ResourceKey<Registry<RaidBoss>> RAID_BOSS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("raid", "boss"));
    private static final Map<RaidTier, Set<RaidBoss>> RAID_BOSS_COLLECTION = new HashMap<>();
    private static final Map<RaidTier, WeightedRandomList<RaidBossEntry>> RAID_BOSSES = new HashMap<>();

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
        RAID_BOSSES.put(tier, WeightedRandomList.create(
            raidBosses.stream().map(raidBoss -> new RaidBossEntry(raidBoss, raidBoss.getWeight())).toList())
        );
    }

    public static RaidBoss getRandomRaidBoss(RandomSource random, RaidTier tier) {
        Optional<RaidBossEntry> raidBossEntry = RaidRegistry.RAID_BOSSES.get(tier).getRandom(random);
        return raidBossEntry.map(RaidBossEntry::boss).orElse(null);
    }

    private record RaidBossEntry(RaidBoss boss, int weight) implements WeightedEntry {
        @Override
        public @NotNull Weight getWeight() {
            return Weight.of(this.weight);
        }
    }
}
