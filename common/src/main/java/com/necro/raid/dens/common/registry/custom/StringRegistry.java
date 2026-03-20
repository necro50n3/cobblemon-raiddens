package com.necro.raid.dens.common.registry.custom;

public class StringRegistry<T> extends CustomRegistry<String, T> {
    public StringRegistry(String defaultKey) {
        super(defaultKey);
    }

    @Override
    public void register(String id, T entry) {
        super.register(id.toLowerCase(), entry);
    }

    @Override
    public T get(String key) {
        return super.get(key.toLowerCase());
    }
}
