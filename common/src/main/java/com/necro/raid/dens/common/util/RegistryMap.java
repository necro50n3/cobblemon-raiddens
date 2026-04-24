package com.necro.raid.dens.common.util;

import com.necro.raid.dens.common.registry.custom.CustomRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class RegistryMap<K, V> {
    private final K[] keys;
    private final V[] values;
    private final Object2IntOpenHashMap<K> indexMap;

    @SuppressWarnings("unchecked")
    public RegistryMap(CustomRegistry<K, ?> registry) {
        registry.freeze();

        this.keys = (K[]) registry.keySet().toArray();
        int size = this.keys.length;
        this.values = (V[]) new Object[size];
        this.indexMap = new Object2IntOpenHashMap<>(size);

        int i = 0;
        for (K key : this.keys) {
            this.indexMap.put(key, i++);
        }
    }

    public void put(K key, V value) {
        int idx = this.indexMap.getInt(key);
        if (idx == -1) throw new IllegalArgumentException("Key not in registry: " + key);
        this.values[idx] = value;
    }

    public V get(K key) {
        int idx = this.indexMap.getInt(key);
        if (idx == -1) return null;

        return this.values[idx];
    }

    public Optional<V> getOptional(K key) {
        int idx = this.indexMap.getInt(key);
        if (idx == -1) return Optional.empty();
        return Optional.ofNullable(this.values[idx]);
    }

    public boolean containsKey(K key) {
        int idx = indexMap.getInt(key);
        return idx != -1 && values[idx] != null;
    }

    public void forEach(BiConsumer<K, V> consumer) {
        for (int i = 0; i < this.values.length; i++) {
            V value = this.values[i];
            if (value != null) consumer.accept(this.keys[i], value);
        }
    }

    public List<K> keySet() {
        List<K> list = new ArrayList<>();
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i] != null) list.add(this.keys[i]);
        }
        return Collections.unmodifiableList(list);
    }

    public List<V> values() {
        List<V> list = new ArrayList<>();
        for (V value : this.values) {
            if (value != null) list.add(value);
        }
        return Collections.unmodifiableList(list);
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        int idx = indexMap.getOrDefault(key, -1);
        if (idx == -1) throw new IllegalArgumentException("Key not in registry: " + key);

        V value = values[idx];
        if (value == null) {
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                values[idx] = newValue;
                return newValue;
            }
            return null;
        }

        return value;
    }

    public int size() {
        return this.values.length;
    }

    public void clear() {
        Arrays.fill(this.values, null);
    }
}