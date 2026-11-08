package com.necro.raid.dens.common.data.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
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
    private final HashSet<String> includeFeatures;
    private Set<ResourceLocation> includeBosses;

    private final HashSet<RaidTier> excludeTiers;
    private final HashSet<RaidType> excludeTypes;
    private final HashSet<String> excludeFeatures;
    private Set<ResourceLocation> excludeBosses;

    private final Boolean adventure;
    private Set<ResourceKey<Biome>> biomes;
    private final double weight;

    private BitSet compiled;
    private ResourceLocation id;

    private final HashSet<String> includeBossesInner;
    private final HashSet<String> excludeBossesInner;
    private final HashSet<String> biomesInner;

    private final boolean isAlwaysValid;

    private RaidBucket(HashSet<RaidTier> includeTiers, HashSet<RaidType> includeTypes, HashSet<String> includeFeatures, HashSet<String> includeBosses,
                       HashSet<RaidTier> excludeTiers, HashSet<RaidType> excludeTypes, HashSet<String> excludeFeatures, HashSet<String> excludeBosses,
                       Boolean adventure, HashSet<String> biomes, double weight) {
        this.includeTiers = includeTiers;
        this.includeTypes = includeTypes;
        this.includeFeatures = includeFeatures;
        this.includeBosses = null;

        this.excludeTiers = excludeTiers;
        this.excludeTypes = excludeTypes;
        this.excludeFeatures = excludeFeatures;
        this.excludeBosses = null;

        this.adventure = adventure;
        this.biomes = null;
        this.weight = weight;

        this.compiled = null;
        this.id = null;

        this.includeBossesInner = includeBosses;
        this.excludeBossesInner = excludeBosses;
        this.biomesInner = biomes;

        this.isAlwaysValid = this.biomesInner.contains("*")
            || (this.biomesInner.contains("#cobblemonraiddens:raid_spawnable") && this.biomesInner.contains("#cobblemonraiddens:raid_spawnable_ignore_sky"));
    }

    private Optional<Boolean> getAdventure() {
        return Optional.ofNullable(this.adventure);
    }

    private HashSet<String> getBiomes() {
        return this.biomesInner;
    }

    public boolean isValidBiome(Holder<Biome> biome, RegistryAccess registryAccess) {
        if (this.isAlwaysValid) return true;
        else if (this.biomesInner.isEmpty()) return false;
        if (this.biomes == null) this.resolveBiomes(registryAccess);
        return biome.unwrapKey()
            .map(this.biomes::contains)
            .orElse(false);
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
        float[] weights = CustomRaidRegistries.RAID_REGISTRY.getWeights(key);
        if (weights == null) return CustomRaidRegistries.RAID_REGISTRY.getRandomRaidBoss(random, level, this.getCompiled(), key);
        else return CustomRaidRegistries.RAID_REGISTRY.roll(random, weights, CustomRaidRegistries.RAID_REGISTRY.getIndices(key));
    }

    private void resolveBiomes(RegistryAccess registryAccess) {
        Set<ResourceKey<Biome>> result = new HashSet<>();
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);

        for (String entry : this.biomesInner) {
            if (entry.equals("*")) {
                result.addAll(biomeRegistry.registryKeySet());
                break;
            }

            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) {
                TagKey<Biome> tag = TagKey.create(Registries.BIOME, id);
                biomeRegistry.getTag(tag).ifPresent(holderSet ->
                    holderSet.forEach(holder -> {
                        if (holder.unwrapKey().isEmpty()) return;
                        result.add(holder.unwrapKey().get());
                    }));
            }
            else {
                ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, id);
                if (biomeRegistry.containsKey(biomeKey)) result.add(biomeKey);
            }
        }

        this.biomes = result;
    }

    private void resolveRaidBosses() {
        Set<ResourceLocation> result = new HashSet<>();
        for (String entry : this.includeBossesInner) {
            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) result.addAll(CustomRaidRegistries.RAID_REGISTRY.getTagEntries(id));
            else if (CustomRaidRegistries.RAID_REGISTRY.exists(id)) result.add(id);
        }
        this.includeBosses = result;

        Set<ResourceLocation> result2 = new HashSet<>();
        for (String entry : this.excludeBossesInner) {
            ResourceLocation id = ResourceLocation.parse(entry.startsWith("#") ? entry.substring(1) : entry);
            if (entry.startsWith("#")) result2.addAll(CustomRaidRegistries.RAID_REGISTRY.getTagEntries(id));
            else if (CustomRaidRegistries.RAID_REGISTRY.exists(id)) result2.add(id);
        }
        this.excludeBosses = result2;
    }

    public BitSet getCompiled() {
        if (this.compiled == null) {
            this.resolveRaidBosses();
            this.compiled = new BitSet();

            boolean hasIncludes = !this.includeTiers.isEmpty() || !this.includeTypes.isEmpty() || !this.includeFeatures.isEmpty();
            boolean hasAnyIncludes = hasIncludes || !this.includeBosses.isEmpty();
            if (hasIncludes || !hasAnyIncludes) this.compiled.set(0, CustomRaidRegistries.RAID_REGISTRY.getAll().size());

            if (!this.includeTiers.isEmpty()) {
                BitSet tierSet = new BitSet();
                for (RaidTier tier : this.includeTiers) {
                    BitSet set = CustomRaidRegistries.RAID_REGISTRY.getBitSet(tier);
                    if (set != null) tierSet.or(set);
                }
                this.compiled.and(tierSet);
            }

            if (!this.includeTypes.isEmpty()) {
                BitSet typeSet = new BitSet();
                for (RaidType type : this.includeTypes) {
                    BitSet set = CustomRaidRegistries.RAID_REGISTRY.getBitSet(type);
                    if (set != null) typeSet.or(set);
                }
                this.compiled.and(typeSet);
            }

            if (!this.includeFeatures.isEmpty()) {
                BitSet featureSet = new BitSet();
                for (String feature : this.includeFeatures) {
                    BitSet set = CustomRaidRegistries.RAID_REGISTRY.getBitSet(feature.toLowerCase(Locale.ROOT));
                    if (set != null) featureSet.or(set);
                }
                this.compiled.and(featureSet);
            }

            if (!this.includeBosses.isEmpty()) {
                BitSet bossSet = new BitSet();
                for (ResourceLocation boss : this.includeBosses) {
                    Integer index = CustomRaidRegistries.RAID_REGISTRY.getIndex(boss);
                    if (index != null) bossSet.set(index);
                }
                this.compiled.or(bossSet);
            }

            for (RaidTier tier : this.excludeTiers) this.compiled.andNot(CustomRaidRegistries.RAID_REGISTRY.getBitSet(tier));
            for (RaidType type : this.excludeTypes) this.compiled.andNot(CustomRaidRegistries.RAID_REGISTRY.getBitSet(type));
            for (String feature : this.excludeFeatures) this.compiled.andNot(CustomRaidRegistries.RAID_REGISTRY.getBitSet(feature.toLowerCase(Locale.ROOT)));
            for (ResourceLocation boss : this.excludeBosses) {
                Integer index = CustomRaidRegistries.RAID_REGISTRY.getIndex(boss);
                if (index != null) this.compiled.clear(index);
            }

            if (this.adventure != null) {
                if (this.adventure) this.compiled.and(CustomRaidRegistries.RAID_REGISTRY.adventureBitSet());
                else this.compiled.andNot(CustomRaidRegistries.RAID_REGISTRY.adventureBitSet());
            }
        }
        return this.compiled;
    }

    private record RaidBucketFilters(HashSet<RaidTier> tiers, HashSet<RaidType> types, HashSet<String> features, HashSet<String> bosses) {
        public RaidBucketFilters() {
            this(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
        }

        private static Codec<RaidBucketFilters> codec() {
            return RecordCodecBuilder.create(inst -> inst.group(
                RaidTier.codec().listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("tier", new HashSet<>()).forGetter(RaidBucketFilters::tiers),
                RaidType.codec().listOf().xmap(HashSet::new, ArrayList::new)
                    .optionalFieldOf("type", new HashSet<>()).forGetter(RaidBucketFilters::types),
                Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new)
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
            Codec.BOOL.optionalFieldOf("adventure").forGetter(RaidBucket::getAdventure),
            Codec.STRING.listOf().xmap(HashSet::new, ArrayList::new).fieldOf("biome").forGetter(RaidBucket::getBiomes),
            Codec.DOUBLE.optionalFieldOf("weight", 10.0).forGetter(RaidBucket::getWeight)
        ).apply(inst, (include, exclude, adventure, biomes, weight) -> new RaidBucket(
            include.tiers(), include.types(), include.features(), include.bosses(),
            exclude.tiers(), exclude.types(), exclude.features(), exclude.bosses(),
            adventure.orElse(null), biomes, weight
        )));
    }
}
