package com.necro.raid.dens.common.registry.custom;

import com.necro.asymmetric.battles.common.util.DoubleWeightedRandomMap;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class RaidBucketRegistry extends CustomRegistry<ResourceLocation, RaidBucket> {
    public RaidBucketRegistry() {
        super(null);
    }

    public @Nullable ResourceLocation getRandom(RandomSource random, Holder<Biome> biome, RegistryAccess registryAccess) {
        Set<ResourceLocation> candidates = new HashSet<>();
        this.registry.forEach((location, bucket) -> {
            if (bucket.isValidBiome(biome, registryAccess)) candidates.add(location);
        });
        if (candidates.isEmpty()) return null;

        DoubleWeightedRandomMap<ResourceLocation> weightedMap = new DoubleWeightedRandomMap<>();
        for (ResourceLocation bucket : candidates) {
            weightedMap.add(bucket, this.get(bucket).getWeight());
        }
        return weightedMap.getRandom(random).orElse(null);
    }
}
