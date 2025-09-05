package com.necro.raid.dens.common.items.model;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.item.RaidHomeBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RaidHomeItemModel extends GeoModel<RaidHomeBlockItem> {
    @Override
    public ResourceLocation getModelResource(RaidHomeBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "geo/raid_crystal_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RaidHomeBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/block/raid_crystal_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RaidHomeBlockItem item) {
        return null;
    }
}
