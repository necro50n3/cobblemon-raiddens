package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidBossAdditions;
import com.necro.raid.dens.common.raids.RaidTier;
import com.necro.raid.dens.common.util.RaidRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BossAdditionsReloadImpl extends AbstractReloadImpl {
    public BossAdditionsReloadImpl() {
        super("raid/boss_additions", DataType.JSON);
    }

    @Override
    protected void preLoad() {}

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        List<ResourceLocation> registry = new ArrayList<>(RaidRegistry.getAll());

        Optional<RaidBossAdditions> additionsOpt = RaidBossAdditions.codec().decode(JsonOps.INSTANCE, object).result().map(Pair::getFirst);
        additionsOpt.ifPresent(additions -> additions.apply(registry));
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load boss additions {}", id, e);
    }

    @Override
    protected void postLoad() {
        RaidRegistry.registerAll();
        RaidTier.updateRandom();
    }
}
