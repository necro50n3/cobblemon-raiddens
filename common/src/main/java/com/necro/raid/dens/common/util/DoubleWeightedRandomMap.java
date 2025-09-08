package com.necro.raid.dens.common.util;

import net.minecraft.util.RandomSource;

import java.util.*;

public class DoubleWeightedRandomMap<T> {
    private final NavigableMap<Double, T> map = new TreeMap<>();
    private double totalWeight = 0.0;

    public void add(T value, double weight) {
        if (weight <= 0) return;
        this.totalWeight += weight;
        this.map.put(this.totalWeight, value);
    }

    public Optional<T> getRandom(RandomSource random) {
        if (this.map.isEmpty() || this.totalWeight <= 0) return Optional.empty();
        double r = random.nextDouble() * this.totalWeight;
        return Optional.ofNullable(this.map.ceilingEntry(r).getValue());
    }

    public void clear() {
        this.map.clear();
        this.totalWeight = 0.0;
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }
}