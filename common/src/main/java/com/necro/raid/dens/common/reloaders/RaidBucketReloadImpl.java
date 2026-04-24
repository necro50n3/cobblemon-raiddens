package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBucket;
import com.necro.raid.dens.common.registry.RaidBucketRegistry;
import net.minecraft.resources.ResourceLocation;

public class RaidBucketReloadImpl extends AbstractReloadImpl {
    public RaidBucketReloadImpl() {
        super("raid/bucket", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        CobblemonRaidDens.LOGGER.info("Registering raid buckets");
        RaidBucketRegistry.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        RaidBucket bucket = RaidBucket.codec().decode(JsonOps.INSTANCE, object).getOrThrow().getFirst();
        bucket.setId(key);
        RaidBucketRegistry.register(bucket);
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid bucket {}", id, e);
    }

    @Override
    protected void postLoad() {
        CobblemonRaidDens.LOGGER.info("Registered {} raid buckets", RaidBucketRegistry.getAll().size());
    }
}
