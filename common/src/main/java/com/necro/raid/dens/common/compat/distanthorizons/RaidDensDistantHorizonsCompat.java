package com.necro.raid.dens.common.compat.distanthorizons;

import net.minecraft.server.level.ServerLevel;

public abstract class RaidDensDistantHorizonsCompat {
    public static RaidDensDistantHorizonsCompat INSTANCE;

    public abstract void unloadLevel(ServerLevel level);
}
