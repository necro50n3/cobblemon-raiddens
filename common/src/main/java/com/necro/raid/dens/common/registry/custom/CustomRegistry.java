package com.necro.raid.dens.common.registry.custom;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Collections;

public class CustomRegistry<K, T> {
    private final Object2ObjectOpenHashMap<K, T> registry;
    private boolean frozen;
    private final K defaultKey;

    public CustomRegistry(K defaultKey) {
        this.registry = new Object2ObjectOpenHashMap<>();
        this.frozen = false;
        this.defaultKey = defaultKey;
    }

    public void register(K id, T entry) {
        if (this.frozen) throw new IllegalStateException("Attempted to add to registry after initialization.");
        this.registry.put(id, entry);
    }

    public void freeze() {
        this.frozen = true;
        this.registry.trim();
    }

    public T get(K key) {
        return this.registry.getOrDefault(key, this.registry.get(this.defaultKey));
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(this.registry.values());
    }
}
