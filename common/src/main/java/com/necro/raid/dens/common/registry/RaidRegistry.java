package com.necro.raid.dens.common.registry;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.registry.store.BinaryRecordStore;
import com.necro.raid.dens.common.registry.store.RegistryCache;
import com.necro.raid.dens.common.util.RegistryMap;
import kotlin.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RaidRegistry {
    private static final long CACHE_EXPIRY_TICKS = 36_000L;
    private static final AtomicReference<Map<ResourceLocation, RaidBoss>> PENDING = new AtomicReference<>(Map.of());

    private static BinaryRecordStore<ResourceLocation, RaidBoss> STORE;
    private static RegistryCache<ResourceLocation, RaidBoss> CACHE;
    private static Map<ResourceLocation, RaidBoss> LOADING = new HashMap<>();

    public static final Map<RaidTier, BitSet> RAIDS_BY_TIER = new EnumMap<>(RaidTier.class);
    public static final Map<RaidType, BitSet> RAIDS_BY_TYPE = new EnumMap<>(RaidType.class);
    public static final RegistryMap<String, BitSet> RAIDS_BY_FEATURE = new RegistryMap<>(CustomRaidRegistries.FEATURE_REGISTRY);
    public static final List<ResourceLocation> RAID_LIST = new ArrayList<>();
    public static final Map<ResourceLocation, Integer> RAID_INDEX = new HashMap<>();
    public static final Map<ResourceLocation, Set<ResourceLocation>> RAID_TAGS = new HashMap<>();
    public static final Map<ResourceLocation, Set<ResourceLocation>> TAG_REVERSE_LOOKUP = new HashMap<>();

    public static final Map<String, float[]> WEIGHTS_CACHE = new HashMap<>();
    public static final Map<String, int[]> INDEX_CACHE = new HashMap<>();
    private static double[] WEIGHTS_BY_INDEX = new double[0];
    private static RaidTier[] TIER_BY_INDEX = new RaidTier[0];
    private static RaidType[] TYPE_BY_INDEX = new RaidType[0];

    public static void register(RaidBoss raidBoss) {
        if (LOADING.put(raidBoss.getId(), raidBoss) == null) {
            int index = RAID_LIST.size();
            RAID_LIST.add(raidBoss.getId());
            RAID_INDEX.put(raidBoss.getId(), index);
        }
    }

    public static void registerAll() {
        int size = RAID_LIST.size();
        WEIGHTS_BY_INDEX = new double[size];
        TIER_BY_INDEX = new RaidTier[size];
        TYPE_BY_INDEX = new RaidType[size];

        for (int index = 0; index < RAID_LIST.size(); index++) {
            RaidBoss raidBoss = LOADING.get(RAID_LIST.get(index));

            RAIDS_BY_TIER.computeIfAbsent(raidBoss.getTier(), tier -> new BitSet()).set(index);
            RAIDS_BY_TYPE.computeIfAbsent(raidBoss.getType(), type -> new BitSet()).set(index);
            RAIDS_BY_FEATURE.computeIfAbsent(raidBoss.getFeature(), feature -> new BitSet()).set(index);

            WEIGHTS_BY_INDEX[index] = raidBoss.getWeight();
            TIER_BY_INDEX[index] = raidBoss.getTier();
            TYPE_BY_INDEX[index] = raidBoss.getType();

            if (raidBoss.getWeight() > 0.0) {
                raidBoss.getTier().setPresent();
                raidBoss.getType().setPresent();
            }
        }

        Map<ResourceLocation, RaidBoss> snapshot = LOADING;
        LOADING = new HashMap<>();
        PENDING.set(snapshot);

        BinaryRecordStore<ResourceLocation, RaidBoss> current = STORE;
        if (current != null) persist(current, snapshot);

        CobblemonRaidDens.LOGGER.info("Registered {} raid bosses", size);
    }

    private static void persist(BinaryRecordStore<ResourceLocation, RaidBoss> target, Map<ResourceLocation, RaidBoss> snapshot) {
        target.rebuildAsync(snapshot.values(), RaidBoss::getId).whenComplete((result, error) -> {
            if (error != null) {
                CobblemonRaidDens.LOGGER.error("Failed to write raid boss storage, keeping bosses in memory", error);
                return;
            }
            PENDING.compareAndSet(snapshot, Map.of());
        });
    }

    public static List<ResourceLocation> getAll() {
        return RAID_LIST;
    }

    public static Collection<RaidBoss> getCache() {
        RegistryCache<ResourceLocation, RaidBoss> current = CACHE;
        if (current == null) return Optional.of(PENDING.get().values()).orElse(List.of());
        return current.getAll();
    }

    public static CompletableFuture<Optional<RaidBoss>> requestRaidBoss(ResourceLocation location) {
        if (location == null || RaidRegistry.isClosed()) return CompletableFuture.completedFuture(Optional.empty());
        RegistryCache<ResourceLocation, RaidBoss> current = CACHE;
        if (current == null) return CompletableFuture.completedFuture(Optional.ofNullable(PENDING.get().get(location)));
        return current.getAsync(location);
    }

    public static void requestRaidBoss(ResourceLocation location, Consumer<RaidBoss> consumer, MinecraftServer server) {
        requestRaidBoss(location, consumer, (id, error) -> {}, server);
    }

    public static void requestRaidBoss(ResourceLocation location, Consumer<RaidBoss> consumer, BiConsumer<ResourceLocation, Throwable> errorConsumer, MinecraftServer server) {
        requestRaidBoss(location).whenCompleteAsync((boss, error) -> {
            if (error != null || boss.isEmpty()) {
                errorConsumer.accept(location, error);
                return;
            }
            consumer.accept(boss.get());
        }, server);
    }

    public static @Nullable RaidBoss getRaidBoss(ResourceLocation location) {
        if (location == null) return null;
        RegistryCache<ResourceLocation, RaidBoss> current = CACHE;
        if (current == null) return PENDING.get().get(location);
        return current.request(location);
    }

    public static @Nullable RaidBoss getLoading(ResourceLocation location) {
        return LOADING.get(location);
    }

    public static Pair<RaidTier, RaidType> getTierAndType(ResourceLocation location) {
        int index = RAID_INDEX.get(location);
        return new Pair<>(TIER_BY_INDEX[index], TYPE_BY_INDEX[index]);
    }

    public static boolean exists(ResourceLocation location) {
        return RAID_INDEX.containsKey(location);
    }

    public static void setTags(Map<ResourceLocation, Set<ResourceLocation>> tags) {
        tags.forEach(RaidRegistry::addTags);
    }

    public static void addTags(ResourceLocation tag, Set<ResourceLocation> bosses) {
        RAID_TAGS.computeIfAbsent(tag, set -> new HashSet<>()).addAll(bosses);
        bosses.forEach(boss -> TAG_REVERSE_LOOKUP.computeIfAbsent(boss, set -> new HashSet<>()).add(tag));
    }

    public static Set<ResourceLocation> getTagsOfBoss(ResourceLocation boss) {
        return new HashSet<>(TAG_REVERSE_LOOKUP.getOrDefault(boss, Set.of()));
    }

    public static boolean isTag(ResourceLocation tag, ResourceLocation boss) {
        if (!RAID_TAGS.containsKey(tag)) return false;
        return RAID_TAGS.get(tag).contains(boss);
    }

    public static Set<ResourceLocation> getTagEntries(ResourceLocation tag) {
        return RAID_TAGS.getOrDefault(tag, new HashSet<>());
    }

    private static float[] buildWeights(int[] matches, Level level) {
        float[] weights = new float[matches.length];
        float sum = 0f;
        for (int i = 0; i < matches.length; i++) {
            int index = matches[i];
            sum += (float) (WEIGHTS_BY_INDEX[index] * TIER_BY_INDEX[index].getWeight(level));
            weights[i] = sum;
        }
        return weights;
    }

    public static ResourceLocation roll(RandomSource random, float[] weights, int[] indexes) {
        float roll = random.nextFloat() * weights[weights.length - 1];
        int idx = Arrays.binarySearch(weights, roll);
        if (idx < 0) idx = -idx - 1;

        return RAID_LIST.get(indexes[idx]);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level, BitSet bitSet, @Nullable String cacheKey) {
        int size = bitSet.cardinality();
        if (size == 0) return null;
        int[] matches = new int[size];
        for (int i = bitSet.nextSetBit(0), idx = 0; i >= 0; i = bitSet.nextSetBit(i + 1), idx++) {
            matches[idx] = i;
        }

        float[] cachedWeights = buildWeights(matches, level);

        if (cacheKey != null) {
            WEIGHTS_CACHE.put(cacheKey, cachedWeights);
            INDEX_CACHE.put(cacheKey, matches);
        }

        return roll(random, cachedWeights, matches);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level, List<RaidTier> tiers, List<RaidType> types, List<String> features) {
        if (tiers == null || tiers.isEmpty()) return null;

        boolean cacheable = (tiers.size() == 1 && (types == null || types.size() <= 1) && (features == null || features.isEmpty()));
        String key = level.dimension().location() + ":" + tiers.getFirst() + ":" + (types == null ? null : types.getFirst());
        if (cacheable && WEIGHTS_CACHE.containsKey(key)) return roll(random, WEIGHTS_CACHE.get(key), INDEX_CACHE.get(key));

        BitSet result = new BitSet();

        for (RaidTier tier : tiers) {
            BitSet set = RAIDS_BY_TIER.get(tier);
            if (set != null) result.or(set);
        }

        if (types != null && !types.isEmpty()) {
            BitSet typeSet = new BitSet();
            for (RaidType type : types) {
                BitSet set = RAIDS_BY_TYPE.get(type);
                if (set != null) typeSet.or(set);
            }
            result.and(typeSet);
        }

        if (features != null && !features.isEmpty()) {
            BitSet featureSet = new BitSet();
            for (String feature : features) {
                BitSet set = RAIDS_BY_FEATURE.get(feature.toLowerCase(Locale.ROOT));
                if (set != null) featureSet.or(set);
            }
            result.and(featureSet);
        }

        return getRandomRaidBoss(random, level, result, cacheable ? key : null);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level, RaidTier tier, RaidType type, String feature) {
        return getRandomRaidBoss(random, level, List.of(tier), type == null ? null : List.of(type), feature == null ? null : List.of(feature));
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level, RaidType type, String feature) {
        return getRandomRaidBoss(random, level, RaidTier.getWeightedRandom(random, level), type, feature);
    }

    public static ResourceLocation getRandomRaidBoss(RandomSource random, Level level) {
        return getRandomRaidBoss(random, level, (RaidType) null, null);
    }

    public static void clear() {
        RAIDS_BY_TIER.values().forEach(BitSet::clear);
        RAIDS_BY_TYPE.values().forEach(BitSet::clear);
        RAIDS_BY_FEATURE.values().forEach(BitSet::clear);
        RAID_LIST.clear();
        RAID_INDEX.clear();

        LOADING.clear();
        RegistryCache<ResourceLocation, RaidBoss> current = CACHE;
        if (current != null) current.invalidateAll();

        WEIGHTS_BY_INDEX = new double[0];
        TIER_BY_INDEX = new RaidTier[0];
        TYPE_BY_INDEX = new RaidType[0];

        WEIGHTS_CACHE.clear();
        INDEX_CACHE.clear();

        for (RaidTier tier : RaidTier.values()) { tier.setPresent(false); }
        for (RaidType type : RaidType.values()) { type.setPresent(false); }
    }

    public static void init(MinecraftServer server) {
        close();
        BinaryRecordStore<ResourceLocation, RaidBoss> newStore = new BinaryRecordStore<>(
            server.getWorldPath(LevelResource.ROOT).resolve("cobblemonraiddens").resolve("bosses.bin"),
            RaidBoss::encode,
            RaidBoss::decode
        );
        STORE = newStore;
        CACHE = new RegistryCache<>(
            id -> loadRaidBoss(newStore, id, server),
            CACHE_EXPIRY_TICKS,
            (id, error) -> CobblemonRaidDens.LOGGER.error("Failed to load raid boss {}", id, error),
            (id, loaded) -> {
                if (!RaidRegistry.isClosed()) RaidDenNetworkMessages.SYNC_BOSS.accept(server, loaded);
            },
            (id, expired) -> {
                if (!RaidRegistry.isClosed()) RaidDenNetworkMessages.REMOVE_SYNCED_BOSS.accept(server, id);
            }
        );

        Map<ResourceLocation, RaidBoss> unsaved = PENDING.get();
        if (!unsaved.isEmpty()) persist(newStore, unsaved);
    }

    private static CompletableFuture<Optional<RaidBoss>> loadRaidBoss(BinaryRecordStore<ResourceLocation, RaidBoss> source, ResourceLocation id, MinecraftServer server) {
        RaidBoss pending = PENDING.get().get(id);
        if (pending != null) return CompletableFuture.completedFuture(Optional.of(pending));
        return source.readAsync(id, server);
    }

    public static void close() {
        RegistryCache<ResourceLocation, RaidBoss> currentCache = CACHE;
        BinaryRecordStore<ResourceLocation, RaidBoss> currentStore = STORE;
        CACHE = null;
        STORE = null;
        if (currentCache != null) currentCache.close();
        if (currentStore != null) currentStore.close();
    }

    public static boolean isClosed() {
        BinaryRecordStore<ResourceLocation, RaidBoss> currentStore = STORE;
        return currentStore == null || currentStore.isClosed();
    }

    public static void tick() {
        RegistryCache<ResourceLocation, RaidBoss> current = CACHE;
        if (current != null) current.tick();
    }
}
