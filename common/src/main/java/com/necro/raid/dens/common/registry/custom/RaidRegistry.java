package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.registry.store.BinaryRecordStore;
import com.necro.raid.dens.common.registry.store.RegistryCache;
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

    private final AtomicReference<Map<ResourceLocation, RaidBoss>> pending = new AtomicReference<>(Map.of());
    private BinaryRecordStore<ResourceLocation, RaidBoss> store;
    private RegistryCache<ResourceLocation, RaidBoss> cache;
    private Map<ResourceLocation, RaidBoss> loading = new HashMap<>();

    private final Map<RaidTier, BitSet> byTier = new EnumMap<>(RaidTier.class);
    private final Map<RaidType, BitSet> byType = new EnumMap<>(RaidType.class);
    private final Map<String, BitSet> byFeature = new HashMap<>();
    private final BitSet byAdventure = new BitSet();
    private final List<ResourceLocation> raidList = new ArrayList<>();
    private final Map<ResourceLocation, Integer> raidIndex = new HashMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> raidTags = new HashMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> tagReverseLookup = new HashMap<>();

    private final Map<String, float[]> weightsCache = new HashMap<>();
    private final Map<String, int[]> indexCache = new HashMap<>();
    private double[] weightsByIndex = new double[0];
    private RaidTier[] tierByIndex = new RaidTier[0];

    public void register(RaidBoss boss) {
        if (this.loading.put(boss.getId(), boss) == null) {
            int index = this.raidList.size();
            this.raidList.add(boss.getId());
            this.raidIndex.put(boss.getId(), index);
        }
    }

    public void registerAll() {
        int size = this.raidList.size();
        this.weightsByIndex = new double[size];
        this.tierByIndex = new RaidTier[size];

        for (int index = 0; index < raidList.size(); index++) {
            RaidBoss boss = loading.get(raidList.get(index));

            this.byTier.computeIfAbsent(boss.getTier(), tier -> new BitSet()).set(index);
            this.byType.computeIfAbsent(boss.getType(), type -> new BitSet()).set(index);
            this.byFeature.computeIfAbsent(boss.getFeature(), feature -> new BitSet()).set(index);
            if (boss.isAdventure()) this.byAdventure.set(index);

            this.weightsByIndex[index] = boss.getWeight();
            this.tierByIndex[index] = boss.getTier();

            if (boss.getWeight() > 0.0) {
                boss.getTier().setPresent();
                boss.getType().setPresent();
            }
        }

        Map<ResourceLocation, RaidBoss> snapshot = this.loading;
        this.loading = new HashMap<>();
        this.pending.set(snapshot);

        BinaryRecordStore<ResourceLocation, RaidBoss> current = this.store;
        if (current != null) this.persist(current, snapshot);

        CobblemonRaidDens.LOGGER.info("Registered {} raid bosses", size);
    }

    private void persist(BinaryRecordStore<ResourceLocation, RaidBoss> target, Map<ResourceLocation, RaidBoss> snapshot) {
        target.rebuildAsync(snapshot.values(), RaidBoss::getId).whenComplete((result, error) -> {
            if (error != null) {
                CobblemonRaidDens.LOGGER.error("Failed to write raid boss storage, keeping bosses in memory", error);
                return;
            }
            this.pending.compareAndSet(snapshot, Map.of());
        });
    }

    public List<ResourceLocation> getAll() {
        return this.raidList;
    }

    public Collection<RaidBoss> getCache() {
        RegistryCache<ResourceLocation, RaidBoss> current = this.cache;
        if (current == null) return Optional.of(this.pending.get().values()).orElse(List.of());
        return current.getAll();
    }

    public boolean cacheHas(ResourceLocation id) {
        RegistryCache<ResourceLocation, RaidBoss> current = this.cache;
        if (current == null) return Optional.of(this.pending.get().containsKey(id)).orElse(false);
        return current.contains(id);
    }

    public CompletableFuture<Optional<RaidBoss>> requestRaidBoss(ResourceLocation location) {
        if (location == null || this.isClosed()) return CompletableFuture.completedFuture(Optional.empty());
        RegistryCache<ResourceLocation, RaidBoss> current = this.cache;
        if (current == null) return CompletableFuture.completedFuture(Optional.ofNullable(this.pending.get().get(location)));
        return current.getAsync(location);
    }

    public void requestRaidBoss(ResourceLocation location, Consumer<RaidBoss> consumer, MinecraftServer server) {
        requestRaidBoss(location, consumer, (id, error) -> {}, server);
    }

    public void requestRaidBoss(ResourceLocation location, Consumer<RaidBoss> consumer, BiConsumer<ResourceLocation, Throwable> errorConsumer, MinecraftServer server) {
        CompletableFuture<Optional<RaidBoss>> future = requestRaidBoss(location);
        BiConsumer<Optional<RaidBoss>, Throwable> handler = (boss, error) -> {
            if (error != null || boss.isEmpty()) errorConsumer.accept(location, error);
            else consumer.accept(boss.get());
        };
        if (server != null) future.whenCompleteAsync(handler, server);
        else future.whenComplete(handler);
    }

    public @Nullable RaidBoss getRaidBoss(ResourceLocation location) {
        if (location == null) return null;
        RegistryCache<ResourceLocation, RaidBoss> current = this.cache;
        if (current == null) return this.pending.get().get(location);
        return current.request(location);
    }

    public @Nullable RaidBoss getLoading(ResourceLocation location) {
        return this.loading.get(location);
    }

    public boolean exists(ResourceLocation location) {
        return this.raidIndex.containsKey(location);
    }

    public void setTags(Map<ResourceLocation, Set<ResourceLocation>> tags) {
        tags.forEach(this::addTags);
    }

    public void addTags(ResourceLocation tag, Set<ResourceLocation> bosses) {
        this.raidTags.computeIfAbsent(tag, set -> new HashSet<>()).addAll(bosses);
        bosses.forEach(boss -> this.tagReverseLookup.computeIfAbsent(boss, set -> new HashSet<>()).add(tag));
    }

    public Set<ResourceLocation> getTagsOfBoss(ResourceLocation boss) {
        return new HashSet<>(this.tagReverseLookup.getOrDefault(boss, Set.of()));
    }

    public boolean isTag(ResourceLocation tag, ResourceLocation boss) {
        if (!this.raidTags.containsKey(tag)) return false;
        return this.raidTags.get(tag).contains(boss);
    }

    public Set<ResourceLocation> getTagEntries(ResourceLocation tag) {
        return this.raidTags.getOrDefault(tag, new HashSet<>());
    }

    public BitSet getBitSet(RaidTier byTier) {
        return this.byTier.get(byTier);
    }

    public BitSet getBitSet(RaidType byType) {
        return this.byType.get(byType);
    }

    public BitSet getBitSet(String byFeature) {
        return this.byFeature.get(byFeature);
    }

    public BitSet adventureBitSet() {
        return this.byAdventure;
    }

    public Integer getIndex(ResourceLocation boss) {
        return this.raidIndex.get(boss);
    }

    public double getWeight(int index) {
        return this.weightsByIndex[index];
    }

    public float[] getWeights(String key) {
        return this.weightsCache.get(key);
    }

    public int[] getIndices(String key) {
        return this.indexCache.get(key);
    }

    private float[] buildWeights(int[] matches, Level level) {
        float[] weights = new float[matches.length];
        float sum = 0f;
        for (int i = 0; i < matches.length; i++) {
            int index = matches[i];
            sum += (float) (this.weightsByIndex[index] * this.tierByIndex[index].getWeight(level));
            weights[i] = sum;
        }
        return weights;
    }

    public ResourceLocation roll(RandomSource random, float[] weights, int[] indexes) {
        float roll = random.nextFloat() * weights[weights.length - 1];
        int idx = Arrays.binarySearch(weights, roll);
        if (idx < 0) idx = -idx - 1;

        return this.raidList.get(indexes[idx]);
    }

    public ResourceLocation getRandomRaidBoss(RandomSource random, Level level, BitSet bitSet, @Nullable String cacheKey) {
        int size = bitSet.cardinality();
        if (size == 0) return null;
        int[] matches = new int[size];
        for (int i = bitSet.nextSetBit(0), idx = 0; i >= 0; i = bitSet.nextSetBit(i + 1), idx++) {
            matches[idx] = i;
        }

        float[] cachedWeights = buildWeights(matches, level);

        if (cacheKey != null) {
            this.weightsCache.put(cacheKey, cachedWeights);
            this.indexCache.put(cacheKey, matches);
        }

        return roll(random, cachedWeights, matches);
    }

    public ResourceLocation getRandomRaidBoss(RandomSource random, Level level, List<RaidTier> tiers, List<RaidType> types, List<String> features, Boolean isAdventure) {
        if (tiers == null || tiers.isEmpty()) return null;

        boolean cacheable = (tiers.size() == 1 && (types == null || types.size() <= 1) && (features == null || features.isEmpty()));
        String key = level.dimension().location() + ":" + tiers.getFirst() + ":" + (types == null ? null : types.getFirst());
        if (cacheable && this.weightsCache.containsKey(key)) return roll(random, this.weightsCache.get(key), this.indexCache.get(key));

        BitSet result = new BitSet();

        for (RaidTier tier : tiers) {
            BitSet set = this.byTier.get(tier);
            if (set != null) result.or(set);
        }

        if (types != null && !types.isEmpty()) {
            BitSet typeSet = new BitSet();
            for (RaidType type : types) {
                BitSet set = this.byType.get(type);
                if (set != null) typeSet.or(set);
            }
            result.and(typeSet);
        }

        if (features != null && !features.isEmpty()) {
            BitSet featureSet = new BitSet();
            for (String feature : features) {
                BitSet set = this.byFeature.get(feature.toLowerCase(Locale.ROOT));
                if (set != null) featureSet.or(set);
            }
            result.and(featureSet);
        }

        if (isAdventure != null) {
            if (isAdventure) result.and(this.byAdventure);
            else result.andNot(this.byAdventure);
        }

        return getRandomRaidBoss(random, level, result, cacheable ? key : null);
    }

    public ResourceLocation getRandomRaidBoss(RandomSource random, Level level, RaidTier tier, RaidType type, String feature, Boolean isAdventure) {
        return getRandomRaidBoss(random, level, List.of(tier), type == null ? null : List.of(type), feature == null ? null : List.of(feature), isAdventure);
    }

    public ResourceLocation getRandomRaidBoss(RandomSource random, Level level, RaidType type, String feature, Boolean isAdventure) {
        return getRandomRaidBoss(random, level, RaidTier.getWeightedRandom(random, level), type, feature, isAdventure);
    }

    public ResourceLocation getRandomRaidBoss(RandomSource random, Level level) {
        return getRandomRaidBoss(random, level, null, null, null);
    }

    public void clear() {
        this.byTier.values().forEach(BitSet::clear);
        this.byType.values().forEach(BitSet::clear);
        this.byFeature.values().forEach(BitSet::clear);
        this.byAdventure.clear();
        this.raidList.clear();
        this.raidIndex.clear();

        this.loading.clear();
        RegistryCache<ResourceLocation, RaidBoss> current = this.cache;
        if (current != null) current.invalidateAll();

        this.weightsByIndex = new double[0];
        this.tierByIndex = new RaidTier[0];

        this.weightsCache.clear();
        this.indexCache.clear();

        for (RaidTier tier : RaidTier.values()) { tier.setPresent(false); }
        for (RaidType type : RaidType.values()) { type.setPresent(false); }
    }

    public void clearReverseLookup() {
        this.tagReverseLookup.clear();
    }

    public void init(MinecraftServer server) {
        this.close();
        BinaryRecordStore<ResourceLocation, RaidBoss> newStore = new BinaryRecordStore<>(
            server.getWorldPath(LevelResource.ROOT).resolve("cobblemonraiddens").resolve("bosses.bin"),
            RaidBoss::encode,
            RaidBoss::decode
        );
        this.store = newStore;
        this.cache = new RegistryCache<>(
            id -> this.loadRaidBoss(newStore, id, server),
            CACHE_EXPIRY_TICKS,
            (id, error) -> CobblemonRaidDens.LOGGER.error("Failed to load raid boss {}", id, error),
            (id, loaded) -> {
                if (!this.isClosed()) RaidDenNetworkMessages.SYNC_BOSS.accept(server, loaded);
            },
            (id, expired) -> {
                if (!this.isClosed()) RaidDenNetworkMessages.REMOVE_SYNCED_BOSS.accept(server, id);
            }
        );

        Map<ResourceLocation, RaidBoss> unsaved = this.pending.get();
        if (!unsaved.isEmpty()) this.persist(newStore, unsaved);
    }

    private CompletableFuture<Optional<RaidBoss>> loadRaidBoss(BinaryRecordStore<ResourceLocation, RaidBoss> source, ResourceLocation id, MinecraftServer server) {
        RaidBoss pending = this.pending.get().get(id);
        if (pending != null) return CompletableFuture.completedFuture(Optional.of(pending));
        return source.readAsync(id, server);
    }

    public void close() {
        RegistryCache<ResourceLocation, RaidBoss> currentCache = this.cache;
        BinaryRecordStore<ResourceLocation, RaidBoss> currentStore = this.store;
        this.cache = null;
        this.store = null;
        if (currentCache != null) currentCache.close();
        if (currentStore != null) currentStore.close();
    }

    public boolean isClosed() {
        BinaryRecordStore<ResourceLocation, RaidBoss> currentStore = this.store;
        return currentStore == null || currentStore.isClosed();
    }

    public void tick() {
        RegistryCache<ResourceLocation, RaidBoss> current = this.cache;
        if (current != null) current.tick();
    }
}
