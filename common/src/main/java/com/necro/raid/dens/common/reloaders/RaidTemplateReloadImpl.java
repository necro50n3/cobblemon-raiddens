package com.necro.raid.dens.common.reloaders;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.registry.RaidDenRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class RaidTemplateReloadImpl extends AbstractReloadImpl {
    public RaidTemplateReloadImpl() {
        super("structure/raid_den", "structure/", DataType.NBT);
    }

    @Override
    protected void preLoad() {
        RaidDenRegistry.clear();
    }

    @Override
    protected void onLoad(ResourceLocation key, CompoundTag nbt) {
        if (!nbt.contains("raid_pois")) return;
        RaidDenRegistry.register(key, nbt.getCompound("raid_pois"));
    }

    @Override
    protected void onError(ResourceLocation id, Exception e) {
        CobblemonRaidDens.LOGGER.error("Failed to load raid den template {}", id, e);
    }

    @Override
    protected void postLoad() {}
}
