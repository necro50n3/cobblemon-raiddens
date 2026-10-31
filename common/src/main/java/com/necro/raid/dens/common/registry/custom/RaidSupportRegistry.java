package com.necro.raid.dens.common.registry.custom;

import com.necro.raid.dens.common.data.support.RaidSupport;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RaidSupportRegistry extends StringRegistry<RaidSupport> {
    private final Set<UUID> supportQueue;

    public RaidSupportRegistry() {
        super(null);
        this.supportQueue = new HashSet<>();
    }

    public void addToQueue(UUID pokemon) {
        this.supportQueue.add(pokemon);
    }

    public boolean removeFromQueue(UUID pokemon) {
        return this.supportQueue.remove(pokemon);
    }
}
