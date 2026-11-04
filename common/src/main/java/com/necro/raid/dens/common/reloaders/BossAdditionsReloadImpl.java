package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBossAdditions;
import com.necro.raid.dens.common.registry.generator.RaidGenerator;
import net.minecraft.resources.ResourceLocation;

public class BossAdditionsReloadImpl extends AbstractReloadImpl {
    public BossAdditionsReloadImpl() {
        super("raid/boss_additions", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        CobblemonRaidDens.LOGGER.info("Registering boss additions");
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        RaidBossAdditions additions = RaidBossAdditions.GSON.fromJson(object, RaidBossAdditions.class);
        additions.applyDefaults();
        RaidGenerator.INSTANCE.add(additions);
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load boss additions {}", id, e);
    }

    @Override
    protected void postLoad() {
        CobblemonRaidDens.LOGGER.info("Registered {} boss additions", RaidGenerator.INSTANCE.additionsSize());
    }
}
