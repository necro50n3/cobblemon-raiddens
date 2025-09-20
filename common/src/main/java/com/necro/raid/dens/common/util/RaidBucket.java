package com.necro.raid.dens.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.raids.RaidFeature;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.core.Holder;
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
    private Set<ResourceLocation> includeBosses;

    private final HashSet<RaidTier> excludeTiers;
    private final HashSet<RaidType> excludeTypes;
    private final HashSet<RaidFeature> excludeFeatures;
    private Set<ResourceLocation> excludeBosses;

    private Set<ResourceKey<Biome>> biomes;
    private final double weight;

    private BitSet compiled;
    private ResourceLocation id;

    private final HashSet<String> includeBossesInner;
    private final HashSet<String> excludeBossesInner;
    private final HashSet<String> biomesInner;

    private RaidBucket(HashSet<RaidTier> includeTiers, HashSet<RaidType> includeTypes, HashSet<RaidFeature> includeFeatures, HashSet<String> includeBosses,
                       HashSet<RaidTier> excludeTiers, HashSet<RaidType> excludeTypes, HashSet<RaidFeature> excludeFeatures, HashSet<String> excludeBosses,
                       HashSet<String> biomes, double weight) {
        this.includeTiers = includeTiers;
        this.includeTypes = includeTypes;
        this.includeFeatures = includeFeatures;
        this.includeBosses = null;

        this.excludeTiers = excludeTiers;
        this.excludeTypes = excludeTypes;
        this.excludeFeatures = excludeFeatures;
        this.excludeBosses = null;

        this.biomes = null;
        this.weight = weight;

        this.compiled = null;
        this.id = null;

        this.includeBossesInner = includeBosses;
        this.excludeBossesInner = excludeBosses;
        this.biomesInner =  biomes;
    }

    private HashSet<String> getBiomes() {
        return this.biomesInner;
    }

    public boolean isValidBiome(Holder<Biome> biome) {
        if (this.biomes == null) this.resolveBiomes();
        return this.biomes.stream().anyMatch(biome::is);
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

    private void resolveBiomes() {
        Set<ResourceKey<Biome>> result = new HashSet<>();

        for (String entry : this.biomesInner) {
            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) {
                TagKey<Biome> tag = TagKey.create(Registries.BIOME, id);
                RaidBucketRegistry.BIOME_REGISTRY.getTag(tag).ifPresent(holderSet ->
                    holderSet.forEach(holder -> {
                        if (holder.unwrapKey().isEmpty()) return;
                        result.add(holder.unwrapKey().get());
                    }));
            } else {
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, id);
                if (RaidBucketRegistry.BIOME_REGISTRY.containsKey(biomeKey)) result.add(biomeKey);
            }
        }

        this.biomes = result;
    }

    private void resolveRaidBosses() {
        Set<ResourceLocation> result = new HashSet<>();
        for (String entry : this.includeBossesInner) {
            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) {
                TagKey<RaidBoss> tag = TagKey.create(RaidRegistry.RAID_BOSS_KEY, id);
                RaidRegistry.REGISTRY.getTag(tag).ifPresent(holderSet ->
                    holderSet.forEach(holder -> {
                        if (holder.unwrapKey().isEmpty()) return;
                        ResourceLocation loc = holder.unwrapKey().get().location();
                        if (RaidRegistry.getRaidBoss(loc) != null) result.add(loc);
                        else if (RaidRegistry.getRaidBoss(holder.value().getId()) != null) result.add(holder.value().getId());
                    }));
            } else {
                if (RaidRegistry.getRaidBoss(id) != null) result.add(id);
            }
        }
        this.includeBosses = result;

        Set<ResourceLocation> result2 = new HashSet<>();
        for (String entry : this.excludeBossesInner) {
            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) {
                TagKey<RaidBoss> tag = TagKey.create(RaidRegistry.RAID_BOSS_KEY, id);
                RaidRegistry.REGISTRY.getTag(tag).ifPresent(holderSet ->
                    holderSet.forEach(holder -> {
                        if (holder.unwrapKey().isEmpty()) return;
                        ResourceLocation loc = holder.unwrapKey().get().location();
                        if (RaidRegistry.getRaidBoss(loc) != null) result2.add(loc);
                        else if (RaidRegistry.getRaidBoss(holder.value().getId()) != null) result2.add(holder.value().getId());
                    }));
            } else {
                if (RaidRegistry.getRaidBoss(id) != null) result2.add(id);
            }
        }
        this.excludeBosses = result2;
    }

    private BitSet getCompiled() {
        if (this.compiled == null) {
            this.resolveRaidBosses();
            this.compiled = new BitSet();

            boolean noIncludes = includeTiers.isEmpty() && includeTypes.isEmpty() && includeFeatures.isEmpty() && includeBosses.isEmpty();
            if (noIncludes) this.compiled.set(0, RaidRegistry.RAID_LIST.size());

            if (!includeTiers.isEmpty()) {
                BitSet tierSet = new BitSet();
                for (RaidTier tier : includeTiers) tierSet.or(RaidRegistry.RAIDS_BY_TIER.get(tier));
                this.compiled.and(tierSet);
            }

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

    private record RaidBucketFilters(HashSet<RaidTier> tiers, HashSet<RaidType> types, HashSet<RaidFeature> features, HashSet<String> bosses) {
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
                Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("boss", new HashSet<>()).forGetter(RaidBucketFilters::bosses)
            ).apply(inst, RaidBucketFilters::new));
        }
    }

    private RaidBucketFilters getIncluded() {
        return new RaidBucketFilters(this.includeTiers, this.includeTypes, this.includeFeatures, this.includeBossesInner);
    }

    private RaidBucketFilters getExcluded() {
        return new RaidBucketFilters(this.excludeTiers, this.excludeTypes, this.excludeFeatures, this.excludeBossesInner);
    }

    public static Codec<RaidBucket> codec() {
        return RecordCodecBuilder.create(inst -> inst.group(
            RaidBucketFilters.codec().optionalFieldOf("include", new RaidBucketFilters()).forGetter(RaidBucket::getIncluded),
            RaidBucketFilters.codec().optionalFieldOf("exclude", new RaidBucketFilters()).forGetter(RaidBucket::getExcluded),
            Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new).fieldOf("biome").forGetter(RaidBucket::getBiomes),
            Codec.DOUBLE.optionalFieldOf("weight", 10.0).forGetter(RaidBucket::getWeight)
        ).apply(inst, (include, exclude, biomes, weight) -> new RaidBucket(
            include.tiers(), include.types(), include.features(), include.bosses(),
            exclude.tiers(), exclude.types(), exclude.features(), exclude.bosses(),
            biomes, weight
        )));
    }
}
