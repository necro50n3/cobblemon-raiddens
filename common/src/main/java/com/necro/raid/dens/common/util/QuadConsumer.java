package com.necro.raid.dens.common.util;

@FunctionalInterface
public interface QuadConsumer<T, U, V, W> {
    void accept(T t, U u, V v, W w);
}

