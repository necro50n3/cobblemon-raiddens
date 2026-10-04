package com.necro.raid.dens.common.registry.store;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class RegistryCache<K, V> implements AutoCloseable {
    private static final long NANOS_PER_TICK = 50_000_000L;
    private static final long CLEANUP_INTERVAL_TICKS = 100L;

    private final Cache<K, V> cache;
    private final ConcurrentHashMap<K, CompletableFuture<Optional<V>>> inFlight = new ConcurrentHashMap<>();
    private final Function<K, CompletableFuture<Optional<V>>> loader;
    private final BiConsumer<K, Throwable> errorHandler;
    private final BiConsumer<K, V> onLoadHandler;
    private final BiConsumer<K, V> onExpireHandler;
    private final AtomicLong gameTicks = new AtomicLong();

    private long epoch;

    public RegistryCache(@NotNull Function<K, CompletableFuture<Optional<V>>> loader, long expireAfterTicks, @NotNull BiConsumer<K, Throwable> errorHandler, @NotNull BiConsumer<K, V> onLoadHandler, @NotNull BiConsumer<K, V> onExpireHandler) {
        this.loader = loader;
        this.errorHandler = errorHandler;
        this.onLoadHandler = onLoadHandler;
        this.onExpireHandler = onExpireHandler;
        if (expireAfterTicks <= 0) throw new IllegalArgumentException("expireAfterTicks must be > 0");

        this.cache = CacheBuilder.newBuilder()
            .expireAfterAccess(expireAfterTicks * NANOS_PER_TICK, TimeUnit.NANOSECONDS)
            .ticker(new Ticker() {
                @Override
                public long read() {
                    return RegistryCache.this.gameTicks.get() * NANOS_PER_TICK;
                }
            })
            .removalListener((RemovalListener<K, V>) notification -> {
                if (notification.getCause() == RemovalCause.EXPIRED) {
                    V value = notification.getValue();
                    if (value != null) this.onExpireHandler.accept(notification.getKey(), value);
                }
            })
            .build();
    }

    public void tick() {
        if (this.gameTicks.incrementAndGet() % CLEANUP_INTERVAL_TICKS == 0) this.cache.cleanUp();
    }

    public V present(K id) {
        return this.cache.getIfPresent(id);
    }

    public V request(K id) {
        V cached = this.cache.getIfPresent(id);
        if (cached == null) getAsync(id);
        return cached;
    }

    public Collection<V> getAll() {
        return this.cache.asMap().values();
    }

    public CompletableFuture<Optional<V>> getAsync(K id) {
        V cached = this.cache.getIfPresent(id);
        if (cached != null) return CompletableFuture.completedFuture(Optional.of(cached));

        CompletableFuture<Optional<V>> created = new CompletableFuture<>();
        CompletableFuture<Optional<V>> existing = this.inFlight.putIfAbsent(id, created);
        if (existing != null) return existing;

        cached = this.cache.getIfPresent(id);
        if (cached != null) {
            this.inFlight.remove(id, created);
            created.complete(Optional.of(cached));
            return created;
        }

        long startEpoch = currentEpoch();

        CompletableFuture<Optional<V>> source;
        try {
            source = this.loader.apply(id);
        }
        catch (RuntimeException e) {
            source = CompletableFuture.failedFuture(e);
        }

        source.whenComplete((value, error) -> {
            if (error == null && value != null && value.isPresent()) {
                V loaded = value.get();
                if (putIfCurrent(id, loaded, startEpoch)) this.onLoadHandler.accept(id, loaded);
            }

            this.inFlight.remove(id, created);

            if (error != null) {
                this.errorHandler.accept(id, error);
                created.completeExceptionally(error);
            }
            else {
                created.complete(value);
            }
        });

        return created;
    }

    public synchronized void invalidateAll() {
        this.epoch++;
        this.cache.invalidateAll();
        this.inFlight.clear();
    }

    public long size() {
        return this.cache.size();
    }

    @Override
    public void close() {
        invalidateAll();
    }

    private synchronized long currentEpoch() {
        return this.epoch;
    }

    private synchronized boolean putIfCurrent(K id, V value, long expectedEpoch) {
        if (this.epoch != expectedEpoch) return false;
        this.cache.put(id, value);
        return true;
    }
}