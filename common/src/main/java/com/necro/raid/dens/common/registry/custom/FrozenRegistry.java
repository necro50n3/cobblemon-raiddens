package com.necro.raid.dens.common.registry.custom;

public class FrozenRegistry<K, T> extends CustomRegistry<K, T> {
    private boolean frozen;

    public FrozenRegistry(K defaultKey) {
        super(defaultKey);
        this.frozen = false;
    }

    @Override
    public void register(K id, T entry) {
        if (this.frozen) throw new IllegalStateException("Attempted to add to registry after initialization.");
        else super.register(id, entry);
    }

    @Override
    public void freeze() {
        if (this.isFrozen()) return;
        this.frozen = true;
        this.registry.trim();
    }

    @Override
    public void clear() {
        this.frozen = false;
        super.clear();
    }

    @Override
    public boolean isFrozen() {
        return this.frozen;
    }
}
