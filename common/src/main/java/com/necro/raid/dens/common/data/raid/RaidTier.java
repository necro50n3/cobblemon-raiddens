package com.necro.raid.dens.common.data.raid;

import com.mojang.serialization.Codec;
import com.necro.asymmetric.battles.common.util.DoubleWeightedRandomMap;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum RaidTier implements StringRepresentable {
    TIER_ONE("tier_one"),
    TIER_TWO("tier_two"),
    TIER_THREE("tier_three"),
    TIER_FOUR("tier_four"),
    TIER_FIVE("tier_five"),
    TIER_SIX("tier_six"),
    TIER_SEVEN("tier_seven");

    private final String id;
    private boolean isPresent;

    private static final double[] DEFAULT = new double[]{9.0, 15.0, 25.0, 25.0, 20.0, 5.0, 1.0};

    RaidTier(String id) {
        this.id = id;
        this.isPresent = false;
    }

    public int getAlpha() {
        return switch (this) {
            case TIER_ONE -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_one;
            case TIER_TWO -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_two;
            case TIER_THREE -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_three;
            case TIER_FOUR -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_four;
            case TIER_FIVE -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_five;
            case TIER_SIX -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_six;
            case TIER_SEVEN -> CobblemonRaidDensClient.CLIENT_CONFIG.beam_strength_tier_seven;
        };
    }

    public String getLootTableId() {
        return "raid/tier/" + this.id;
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

    public static RaidTier getWeightedRandom(RandomSource random, String dimension, List<RaidTier> tiers) {
        double[] weights = CobblemonRaidDens.CONFIG.dimension_tier_weights.getOrDefault(dimension, DEFAULT);
        if (weights.length == 0) weights = DEFAULT;
        DoubleWeightedRandomMap<RaidTier> map = new DoubleWeightedRandomMap<>();
        for (RaidTier tier : tiers) {
            int ordinal = tier.ordinal();
            if (ordinal >= weights.length) ordinal = weights.length - 1;
            map.add(tier, weights[ordinal]);
        }
        return map.getRandom(random).orElse(RaidTier.TIER_ONE);
    }

    public static RaidTier getWeightedRandom(RandomSource random, Level level, List<RaidTier> tiers) {
        String levelKey = level.dimension().location().toString();
        return RaidTier.getWeightedRandom(random, levelKey, tiers);
    }

    public static RaidTier getWeightedRandom(RandomSource random, Level level) {
        return RaidTier.getWeightedRandom(random, level, List.of(RaidTier.values()));
    }

    public static RaidTier fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static Codec<RaidTier> codec() {
        return Codec.STRING.xmap(RaidTier::fromString, Enum::name);
    }
}
