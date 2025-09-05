package com.necro.raid.dens.common.items.model;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RaidCrystalItemModel extends GeoModel<RaidCrystalBlockItem> {
    @Override
    public ResourceLocation getModelResource(RaidCrystalBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "geo/raid_crystal_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RaidCrystalBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/block/raid_crystal_block_stellar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RaidCrystalBlockItem item) {
        return null;
    }
}
