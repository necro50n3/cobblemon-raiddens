package com.necro.raid.dens.common.registry;

import com.necro.raid.dens.common.util.DoubleWeightedRandomMap;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RaidBucketRegistry {
    public static final ResourceKey<Registry<RaidBucket>> BUCKET_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("raid", "bucket"));
    public static Registry<RaidBucket> REGISTRY;
    public static Registry<Biome> BIOME_REGISTRY;

    private static final Map<ResourceLocation, RaidBucket> BUCKET_MAP = new HashMap<>();

    public static void register(RaidBucket bucket) {
        BUCKET_MAP.put(bucket.getId(), bucket);
    }

    public static ResourceLocation getRandomBucket(RandomSource random, Holder<Biome> biome) {
        Set<ResourceLocation> candidates = new HashSet<>();
        BUCKET_MAP.forEach((location, bucket) -> {
            if (bucket.isValidBiome(biome)) candidates.add(location);
        });
        if (candidates.isEmpty()) return null;

        DoubleWeightedRandomMap<ResourceLocation> weightedMap = new DoubleWeightedRandomMap<>();
        for (ResourceLocation bucket : candidates) {
            weightedMap.add(bucket, getBucket(bucket).getWeight());
        }
        return weightedMap.getRandom(random).orElse(null);
    }

    public static RaidBucket getBucket(ResourceLocation bucket) {
        return BUCKET_MAP.getOrDefault(bucket, null);
    }

    public static void clear() {
        BUCKET_MAP.clear();
    }

    public static void init(MinecraftServer server) {
        REGISTRY = server.registryAccess().registryOrThrow(BUCKET_KEY);
        BIOME_REGISTRY = server.registryAccess().registryOrThrow(Registries.BIOME);
    }
}
