package com.necro.raid.dens.common.raids;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum RaidTier implements StringRepresentable {
    TIER_ONE("tier_one", RaidTier.initHealth(0), 0, 12),
    TIER_TWO("tier_two", RaidTier.initHealth(1), 1, 20),
    TIER_THREE("tier_three", RaidTier.initHealth(2), 2, 35),
    TIER_FOUR("tier_four", RaidTier.initHealth(3), 3, 45),
    TIER_FIVE("tier_five", RaidTier.initHealth(4), 4, 75),
    TIER_SIX("tier_six", RaidTier.initHealth(5), 5, 75),
    TIER_SEVEN("tier_seven", RaidTier.initHealth(6), 6, 100);

    private final String id;
    private final int health;
    private final int maxIvs;
    private final int level;
    private boolean isPresent;

    private static final NavigableMap<Double, RaidTier> RANDOM_MAP = new TreeMap<>();
    private static double RANDOM_TOTAL;


    RaidTier(String id, int health, int maxIvs, int level) {
        this.id = id;
        this.health = health;
        this.maxIvs = maxIvs;
        this.level = level;
        this.isPresent = false;
    }

    public String getLootTableId() {
        return "raids/tiers/" + this.id;
    }

    public int getHealth() {
        return this.health;
    }

    public int getMaxIvs() {
        return this.maxIvs;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void setPresent() {
        this.isPresent = true;
    }

    public String getStars() {
        return switch (this) {
            case TIER_ONE -> "★";
            case TIER_TWO -> "★★";
            case TIER_THREE -> "★★★";
            case TIER_FOUR -> "★★★★";
            case TIER_FIVE -> "★★★★★";
            case TIER_SIX -> "★★★★★★";
            case TIER_SEVEN -> "★★★★★★★";
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public static void updateRandom() {
        List<Double> weights = new ArrayList<>(Arrays.stream(CobblemonRaidDens.CONFIG.tier_weights).boxed().toList());
        while (weights.size() < RaidTier.values().length) {
            weights.add(CobblemonRaidDens.CONFIG.tier_weights[CobblemonRaidDens.CONFIG.tier_weights.length - 1]);
        }

        double total = 0;
        for (int i = 0; i < weights.size(); i++) {
            RaidTier tier = RaidTier.values()[i];
            if (!tier.isPresent()) continue;
            total += weights.get(i);
            RANDOM_MAP.put(total, RaidTier.values()[i]);
        }
        RANDOM_TOTAL = total;
    }

    public static RaidTier getWeightedRandom(RandomSource random) {
        if (RANDOM_MAP.isEmpty()) RaidTier.updateRandom();
        if (RANDOM_MAP.isEmpty()) return RaidTier.TIER_ONE;
        double rand = random.nextDouble() * RANDOM_TOTAL;
        return RANDOM_MAP.higherEntry(rand).getValue();
    }

    public static int initHealth(int index) {
        index = Math.min(index, CobblemonRaidDens.CONFIG.tier_health_multiplier.length - 1);
        return CobblemonRaidDens.CONFIG.tier_health_multiplier[index];
    }

    public static RaidTier fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static Codec<RaidTier> codec() {
        return Codec.STRING.xmap(RaidTier::fromString, Enum::name);
    }
}
