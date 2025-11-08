package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.RaidBucket;
import com.necro.raid.dens.common.util.RaidBucketRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RaidBucketReloadImpl extends AbstractReloadImpl {
    public RaidBucketReloadImpl() {
        super("raid/bucket", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        RaidBucketRegistry.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        Optional<RaidBucket> bucketOpt = RaidBucket.codec().decode(JsonOps.INSTANCE, object).result().map(Pair::getFirst);
        bucketOpt.ifPresent(bucket -> {
            bucket.setId(key);
            RaidBucketRegistry.register(bucket);
        });
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid bucket {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
