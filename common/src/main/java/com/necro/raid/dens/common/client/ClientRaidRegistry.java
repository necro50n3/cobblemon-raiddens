package com.necro.raid.dens.common.client;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ClientRaidRegistry {
    private static final Map<ResourceLocation, RaidBoss> CLIENT_RAID_LOOKUP = new HashMap<>();

    public static void register(RaidBoss raidBoss) {
        CLIENT_RAID_LOOKUP.put(raidBoss.getId(), raidBoss);
    }

    public static RaidBoss getRaidBoss(ResourceLocation location) {
        return CLIENT_RAID_LOOKUP.get(location);
    }

    public static void clear() {
        CLIENT_RAID_LOOKUP.clear();
    }
}
