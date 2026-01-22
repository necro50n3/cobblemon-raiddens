package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.raid.RaidBossAdditions;
import com.necro.raid.dens.common.data.raid.RaidTier;
import com.necro.raid.dens.common.registry.RaidRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BossAdditionsReloadImpl extends AbstractReloadImpl {
    private List<ResourceLocation> registry;
    private final List<RaidBossAdditions> additionsList;

    public BossAdditionsReloadImpl() {
        super("raid/boss_additions", DataType.JSON);
        this.registry = null;
        this.additionsList = new ArrayList<>();
    }

    @Override
    protected void preLoad() {}

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        if (this.registry == null) this.registry = new ArrayList<>(RaidRegistry.getAll());
        RaidBossAdditions additions = RaidBossAdditions.GSON.fromJson(object, RaidBossAdditions.class);
        this.additionsList.add(additions);
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load boss additions {}", id, e);
    }

    @Override
    protected void postLoad() {
        this.additionsList.sort(Comparator.comparingInt(RaidBossAdditions::priority).reversed());
        this.additionsList.forEach(additions -> additions.apply(this.registry));

        RaidRegistry.registerAll();
        RaidTier.updateRandom();
    }
}
