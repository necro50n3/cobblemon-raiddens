package com.necro.raid.dens.common.reloaders;

import com.google.gson.JsonObject;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.data.adventure.RaidAdventure;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import net.minecraft.resources.ResourceLocation;

public class RaidAdventureReloadImpl extends AbstractReloadImpl {
    public RaidAdventureReloadImpl() {
        super("raid/adventure", DataType.JSON);
    }

    @Override
    protected void preLoad() {
        CobblemonRaidDens.LOGGER.info("Registering raid adventures");
        CustomRaidRegistries.ADVENTURE_REGISTRY.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, JsonObject object) {
        RaidAdventure adventure = RaidAdventure.GSON.fromJson(object, RaidAdventure.class);
        adventure.instance().setId(key);
        adventure.applyDefaults();
        CustomRaidRegistries.ADVENTURE_REGISTRY.register(key, adventure);
    }

    @Override
    protected ResourceLocation createKey(ResourceLocation id) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace(this.idRemove, "").replace(this.suffix(), "") + "_adventure");
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid adventure {}", id, e);
    }

    @Override
    protected void postLoad() {
        CobblemonRaidDens.LOGGER.info("Registered {} raid adventures", CustomRaidRegistries.ADVENTURE_REGISTRY.keySet().size());
    }
}
