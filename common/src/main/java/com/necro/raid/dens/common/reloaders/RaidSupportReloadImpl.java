package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.support.RaidSupport;
import com.necro.raid.dens.common.registry.RaidSupportRegistry;
import net.minecraft.resources.ResourceLocation;

public class RaidSupportReloadImpl extends AbstractReloadImpl {
    public RaidSupportReloadImpl() {
        super("raid/support", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        RaidSupportRegistry.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        RaidSupport raidSupport = RaidSupport.codec().decode(JsonOps.INSTANCE, object).getOrThrow().getFirst();
        RaidSupportRegistry.register(raidSupport);
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid support {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
