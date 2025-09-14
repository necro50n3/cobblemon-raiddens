package com.necro.raid.dens.neoforge.compat.distanthorizons;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.distanthorizons.RaidDensDistantHorizonsCompat;
import com.seibel.distanthorizons.core.api.internal.SharedApi;
import loaderCommon.neoforge.com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper;
import net.minecraft.server.level.ServerLevel;

public class NeoForgeDistantHorizonsCompat extends RaidDensDistantHorizonsCompat {
    @Override
    public void unloadLevel(ServerLevel level) {
        try { SharedApi.getIDhServerWorld().getLevel(ServerLevelWrapper.getWrapper(level)).close(); }
        catch (Exception e) { CobblemonRaidDens.LOGGER.error("Error closing DH level: ", e); }
    }

    public static void init() {
        INSTANCE = new NeoForgeDistantHorizonsCompat();
    }
}
