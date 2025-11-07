package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.structure.RaidDenPool;
import com.necro.raid.dens.common.util.RaidDenRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RaidDenPoolReloadImpl extends AbstractReloadImpl {
    public RaidDenPoolReloadImpl() {
        super("raid/den_pool", DataType.JSON);
    }

    @Override
    protected void preLoad() {}

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        Optional<RaidDenPool> denOpt = RaidDenPool.codec().decode(JsonOps.INSTANCE, object).result().map(Pair::getFirst);
        denOpt.ifPresent(denPool -> {
            denPool.setId(key);
            RaidDenRegistry.register(denPool);
        });
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid den pool {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
