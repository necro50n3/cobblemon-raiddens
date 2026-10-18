package com.necro.raid.dens.common.util;

import net.minecraft.util.RandomSource;

import java.util.*;

public class DoubleWeightedRandomMap<T> {
    private final NavigableMap<Double, T> map = new TreeMap<>();
    private final Map<T, Double> weights = new HashMap<>();
    private double totalWeight = 0.0;
    private final boolean replace;

    public DoubleWeightedRandomMap(boolean replace) {
        this.replace = replace;
    }

    public DoubleWeightedRandomMap() {
        this(true);
    }

    public void add(T value, double weight) {
        if (!(weight > 0)) return;
        if (this.weights.containsKey(value)) this.remove(value);
        this.totalWeight += weight;
        this.map.put(this.totalWeight, value);
        this.weights.put(value, weight);
    }

    public double getWeight(T value) {
        return this.weights.getOrDefault(value, 0.0);
    }

    public Optional<T> getRandom(RandomSource random) {
        return this.getRandom(random.nextDouble());
    }

    public Optional<T> getRandom(double random) {
        if (this.map.isEmpty() || this.totalWeight <= 0) return Optional.empty();
        double r = random * this.totalWeight;
        Map.Entry<Double, T> found = this.map.ceilingEntry(r);
        T entry = (found != null ? found : this.map.lastEntry()).getValue();
        if (!this.replace) this.remove(entry);
        return Optional.ofNullable(entry);
    }

    public boolean remove(T value) {
        if (this.weights.remove(value) == null) return false;

        this.map.clear();
        this.totalWeight = 0.0;
        for (Map.Entry<T, Double> e : this.weights.entrySet()) {
            this.totalWeight += e.getValue();
            this.map.put(this.totalWeight, e.getKey());
        }
        return true;
    }
}