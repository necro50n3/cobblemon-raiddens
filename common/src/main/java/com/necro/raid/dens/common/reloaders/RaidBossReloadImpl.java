package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidBoss;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RaidBossReloadImpl extends AbstractReloadImpl {
    public RaidBossReloadImpl() {
        super("raid/boss", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        RaidRegistry.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        Optional<RaidBoss> raidBossOpt = RaidBoss.codec().decode(JsonOps.INSTANCE, object).result().map(Pair::getFirst);
        raidBossOpt.ifPresent(raidBoss -> {
            raidBoss.setId(key);
            RaidRegistry.register(raidBoss);
        });
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid boss {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
