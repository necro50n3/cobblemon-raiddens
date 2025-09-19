package com.necro.raid.dens.common.raids;

import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum RaidTier implements StringRepresentable {
    TIER_ONE("tier_one", initHealth(0), initIvs(0), initLevel(0)),
    TIER_TWO("tier_two", initHealth(1), initIvs(1), initLevel(1)),
    TIER_THREE("tier_three", initHealth(2), initIvs(2), initLevel(2)),
    TIER_FOUR("tier_four", initHealth(3), initIvs(3), initLevel(3)),
    TIER_FIVE("tier_five", initHealth(4), initIvs(4), initLevel(4)),
    TIER_SIX("tier_six", initHealth(5), initIvs(5), initLevel(5)),
    TIER_SEVEN("tier_seven", initHealth(6), initIvs(6), initLevel(6));

    private final String id;
    private final int health;
    private final int maxIvs;
    private final int level;
    private boolean isPresent;

    private static final Map<String, DoubleWeightedRandomMap<RaidTier>> RANDOM_MAP = new HashMap<>();

    RaidTier(String id, int health, int maxIvs, int level) {
        this.id = id;
        this.health = health;
        this.maxIvs = maxIvs;
        this.level = level;
        this.isPresent = false;
    }

    public String getLootTableId() {
        return "raid/tier/" + this.id;
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

    public void setPresent(boolean isPresent) {
        this.isPresent = isPresent;
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
        RANDOM_MAP.clear();

        for (Map.Entry<String, double[]> entry : CobblemonRaidDens.CONFIG.dimension_tier_weights.entrySet()) {
            RaidTier.addWeightedMap(entry.getKey(), entry.getValue());
        }

        if (!RANDOM_MAP.containsKey("minecraft:overworld")) {
            double[] weights;
            if (!CobblemonRaidDens.CONFIG.dimension_tier_weights.isEmpty()) {
                weights = CobblemonRaidDens.CONFIG.dimension_tier_weights.values().iterator().next();
            } else {
                weights = new double[]{9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0};
            }
            RaidTier.addWeightedMap("minecraft:overworld", weights);
        }
    }

    private static void addWeightedMap(String dimension, double[] tierWeights) {
        DoubleWeightedRandomMap<RaidTier> weightedMap = new DoubleWeightedRandomMap<>();

        List<Double> weights = new ArrayList<>(Arrays.stream(tierWeights).boxed().toList());
        while (weights.size() < RaidTier.values().length) {
            weights.add(tierWeights[tierWeights.length - 1]);
        }

        for (int i = 0; i < weights.size(); i++) {
            RaidTier tier = RaidTier.values()[i];
            if (!tier.isPresent()) continue;
            weightedMap.add(RaidTier.values()[i], weights.get(i));
        }

        RANDOM_MAP.put(dimension, weightedMap);
    }

    public static RaidTier getWeightedRandom(RandomSource random, String dimension) {
        if (RANDOM_MAP.isEmpty()) RaidTier.updateRandom();
        if (RANDOM_MAP.isEmpty()) return RaidTier.TIER_ONE;
        else if (!RANDOM_MAP.containsKey(dimension)) dimension = "minecraft:overworld";
        Optional<RaidTier> raidTier = RANDOM_MAP.get(dimension).getRandom(random);
        return raidTier.orElse(RaidTier.TIER_ONE);
    }

    public static RaidTier getWeightedRandom(RandomSource random, Level level) {
        String levelKey = level.dimension().location().toString();
        return RaidTier.getWeightedRandom(random, levelKey);
    }

    public double getWeight(Level level) {
        String levelKey = level.dimension().location().toString();
        if (!RANDOM_MAP.containsKey(levelKey)) levelKey = "minecraft:overworld";
        return RANDOM_MAP.get(levelKey).getWeight(this);
    }

    public static int initHealth(int index) {
        index = Math.min(index, CobblemonRaidDens.CONFIG.tier_health_multiplier.length - 1);
        return CobblemonRaidDens.CONFIG.tier_health_multiplier[index];
    }

    public static int initIvs(int index) {
        index = Math.min(index, CobblemonRaidDens.CONFIG.tier_ivs.length - 1);
        return CobblemonRaidDens.CONFIG.tier_ivs[index];
    }

    public static int initLevel(int index) {
        index = Math.min(index, CobblemonRaidDens.CONFIG.tier_level.length - 1);
        return CobblemonRaidDens.CONFIG.tier_level[index];
    }

    public static RaidTier fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static Codec<RaidTier> codec() {
        return Codec.STRING.xmap(RaidTier::fromString, Enum::name);
    }
}
