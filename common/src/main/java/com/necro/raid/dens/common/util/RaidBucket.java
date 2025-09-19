package com.necro.raid.dens.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.*;

public class RaidBucket {
    private final HashSet<RaidTier> includeTiers;
    private final HashSet<RaidType> includeTypes;
    private final HashSet<RaidFeature> includeFeatures;
    private final HashSet<ResourceLocation> includeBosses;

    private final HashSet<RaidTier> excludeTiers;
    private final HashSet<RaidType> excludeTypes;
    private final HashSet<RaidFeature> excludeFeatures;
    private final HashSet<ResourceLocation> excludeBosses;

    private final HashSet<String> biomes;
    private Set<ResourceKey<Biome>> resolvedBiomes;
    private final double weight;

    private BitSet compiled;
    private ResourceLocation id;

    private RaidBucket(HashSet<RaidTier> includeTiers, HashSet<RaidType> includeTypes, HashSet<RaidFeature> includeFeatures, HashSet<ResourceLocation> includeBosses,
                       HashSet<RaidTier> excludeTiers, HashSet<RaidType> excludeTypes, HashSet<RaidFeature> excludeFeatures, HashSet<ResourceLocation> excludeBosses,
                       HashSet<String> biomes, double weight) {
        this.includeTiers = includeTiers;
        this.includeTypes = includeTypes;
        this.includeFeatures = includeFeatures;
        this.includeBosses = includeBosses;

        this.excludeTiers = excludeTiers;
        this.excludeTypes = excludeTypes;
        this.excludeFeatures = excludeFeatures;
        this.excludeBosses = excludeBosses;

        this.biomes =  biomes;
        this.resolvedBiomes = null;
        this.weight = weight;

        this.compiled = null;
        this.id = null;
    }

    private HashSet<String> getBiomes() {
        return this.biomes;
    }

    public boolean isValidBiome(Registry<Biome> biomeRegistry, Holder<Biome> biome) {
        if (this.resolvedBiomes == null) this.resolveBiomes(biomeRegistry);
        return this.resolvedBiomes.isEmpty() || this.resolvedBiomes.stream().anyMatch(biome::is);
    }

    public double getWeight() {
        return this.weight;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public void setId(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getRandomRaidBoss(RandomSource random, Level level) {
        String key = level.dimension().location() + ":" + this.getId().toString();
        if (RaidRegistry.WEIGHTS_CACHE.containsKey(key))
            return RaidRegistry.roll(random, RaidRegistry.WEIGHTS_CACHE.get(key), RaidRegistry.INDEX_CACHE.get(key));
        else return RaidRegistry.getRandomRaidBoss(random, level, this.getCompiled(), key);
    }

    public void resolveBiomes(Registry<Biome> biomeRegistry) {
        Set<ResourceKey<Biome>> result = new HashSet<>();

        for (String entry : this.biomes) {
            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) {
                TagKey<Biome> tag = TagKey.create(Registries.BIOME, id);
                biomeRegistry.getTag(tag).ifPresent(holderSet ->
                    holderSet.forEach(holder -> {
                        if (holder.unwrapKey().isEmpty()) return;
                        result.add(holder.unwrapKey().get());
                    }));
            } else {
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, id);
                if (biomeRegistry.containsKey(biomeKey)) {
                    result.add(biomeKey);
                }
            }
        }

        this.resolvedBiomes = result;
    }

    private BitSet getCompiled() {
        if (this.compiled == null) {
            this.compiled = new BitSet();

            if (includeTiers.isEmpty()) this.compiled.set(0, RaidRegistry.RAID_LIST.size());
            else for (RaidTier tier : includeTiers) this.compiled.or(RaidRegistry.RAIDS_BY_TIER.get(tier));

            if (!includeTypes.isEmpty()) {
                BitSet typeSet = new BitSet();
                for (RaidType type : includeTypes) typeSet.or(RaidRegistry.RAIDS_BY_TYPE.get(type));
                this.compiled.and(typeSet);
            }

            if (!includeFeatures.isEmpty()) {
                BitSet featureSet = new BitSet();
                for (RaidFeature feature : includeFeatures) featureSet.or(RaidRegistry.RAIDS_BY_FEATURE.get(feature));
                this.compiled.and(featureSet);
            }

            if (!this.includeBosses.isEmpty()) {
                BitSet bossSet = new BitSet();
                for (ResourceLocation raidBoss : this.includeBosses) {
                    Integer index = RaidRegistry.RAID_INDEX.get(raidBoss);
                    if (index != null) bossSet.set(index);
                }
                this.compiled.or(bossSet);
            }

            for (RaidTier tier : excludeTiers) this.compiled.andNot(RaidRegistry.RAIDS_BY_TIER.get(tier));
            for (RaidType type : excludeTypes) this.compiled.andNot(RaidRegistry.RAIDS_BY_TYPE.get(type));
            for (RaidFeature feature : excludeFeatures) this.compiled.andNot(RaidRegistry.RAIDS_BY_FEATURE.get(feature));
            for (ResourceLocation raidBoss : excludeBosses) {
                Integer index = RaidRegistry.RAID_INDEX.get(raidBoss);
                if (index != null) this.compiled.clear(index);
            }
        }
        return this.compiled;
    }

    private record RaidBucketFilters(HashSet<RaidTier> tiers, HashSet<RaidType> types, HashSet<RaidFeature> features, HashSet<ResourceLocation> bosses) {
        public RaidBucketFilters() {
            this(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
        }

        private static Codec<RaidBucketFilters> codec() {
            return RecordCodecBuilder.create(inst -> inst.group(
                RaidTier.codec().listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("tier", new HashSet<>()).forGetter(RaidBucketFilters::tiers),
                RaidType.codec().listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("type", new HashSet<>()).forGetter(RaidBucketFilters::types),
                RaidFeature.codec().listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("feature", new HashSet<>()).forGetter(RaidBucketFilters::features),
                ResourceLocation.CODEC.listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("boss", new HashSet<>()).forGetter(RaidBucketFilters::bosses)
            ).apply(inst, RaidBucketFilters::new));
        }
    }

    private RaidBucketFilters getIncluded() {
        return new RaidBucketFilters(this.includeTiers, this.includeTypes, this.includeFeatures, this.includeBosses);
    }

    private RaidBucketFilters getExcluded() {
        return new RaidBucketFilters(this.excludeTiers, this.excludeTypes, this.excludeFeatures, this.excludeBosses);
    }

    public static Codec<RaidBucket> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            RaidBucketFilters.codec().optionalFieldOf("include", new RaidBucketFilters()).forGetter(RaidBucket::getIncluded),
            RaidBucketFilters.codec().optionalFieldOf("exclude", new RaidBucketFilters()).forGetter(RaidBucket::getExcluded),
            Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new)
                .optionalFieldOf("biomes", new HashSet<>()).forGetter(RaidBucket::getBiomes),
            Codec.DOUBLE.optionalFieldOf("weight", 10.0).forGetter(RaidBucket::getWeight)
        ).apply(inst, (include, exclude, biomes, weight) -> new RaidBucket(
            include.tiers(), include.types(), include.features(), include.bosses(),
            exclude.tiers(), exclude.types(), exclude.features(), exclude.bosses(),
            biomes, weight
        )));
    }
}
