package com.necro.raid.dens.common.registry.custom;

import java.util.Locale;

public class StringRegistry<T> extends CustomRegistry<String, T> {
    public StringRegistry(String defaultKey) {
        super(defaultKey);
    }

    @Override
    public void register(String id, T entry) {
        super.register(id.toLowerCase(Locale.ROOT), entry);
    }

    @Override
    public T get(String key) {
        return super.get(key.toLowerCase(Locale.ROOT));
    }
}
