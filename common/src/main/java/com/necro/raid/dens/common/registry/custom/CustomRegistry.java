package com.necro.raid.dens.common.registry.custom;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Collections;

public class CustomRegistry<K, T> {
    private final Object2ObjectOpenHashMap<K, T> registry;
    private final K defaultKey;

    public CustomRegistry(K defaultKey) {
        this.registry = new Object2ObjectOpenHashMap<>();
        this.defaultKey = defaultKey;
    }

    public void register(K id, T entry) {
        if (id == null) throw new IllegalArgumentException("Cannot register null key");
        this.registry.put(id, entry);
    }

    public T get(K key) {
        return this.registry.getOrDefault(key, this.registry.get(this.defaultKey));
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(this.registry.values());
    }
}
