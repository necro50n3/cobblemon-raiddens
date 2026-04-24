package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.structure.RaidDenPool;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import net.minecraft.resources.ResourceLocation;

public class RaidDenPoolReloadImpl extends AbstractReloadImpl {
    public RaidDenPoolReloadImpl() {
        super("raid/den_pool", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        CobblemonRaidDens.LOGGER.info("Registering raid den pools");
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        RaidDenPool denPool = RaidDenPool.codec().decode(JsonOps.INSTANCE, object).getOrThrow().getFirst();
        denPool.setId(key);
        RaidDenRegistry.register(denPool);
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid den pool {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
