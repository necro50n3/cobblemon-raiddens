package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import net.minecraft.resources.ResourceLocation;

public class RaidBossReloadImpl extends AbstractReloadImpl {
    public RaidBossReloadImpl() {
        super("raid/boss", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        CobblemonRaidDens.LOGGER.info("Registering raid bosses");
        CustomRaidRegistries.RAID_REGISTRY.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        RaidBoss raidBoss = RaidBoss.GSON.fromJson(object, RaidBoss.class);
        raidBoss.setId(key);
        raidBoss.applyDefaults();
        CustomRaidRegistries.RAID_REGISTRY.register(raidBoss);
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid raidBoss {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
