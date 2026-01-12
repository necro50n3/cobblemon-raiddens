package com.necro.raid.dens.common.registry;

import com.necro.raid.dens.common.data.support.RaidSupport;

import java.util.*;

public class RaidSupportRegistry {
    public static final Map<String, RaidSupport> SUPPORT_REGISTRY = new HashMap<>();
    public static final Set<UUID> SUPPORT_QUEUE = new HashSet<>();

    public static void register(RaidSupport support) {
        SUPPORT_REGISTRY.put(support.move(), support);
    }

    public static RaidSupport getSupport(String support) {
        return SUPPORT_REGISTRY.getOrDefault(support, null);
    }

    public static void addToQueue(UUID pokemon) {
        SUPPORT_QUEUE.add(pokemon);
    }

    public static boolean removeFromQueue(UUID pokemon) {
        return SUPPORT_QUEUE.remove(pokemon);
    }

    public static void clear() {
        SUPPORT_REGISTRY.clear();
    }
}
