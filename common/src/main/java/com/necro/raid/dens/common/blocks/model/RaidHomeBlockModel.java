package com.necro.raid.dens.common.blocks.model;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RaidHomeBlockModel extends GeoModel<RaidHomeBlockEntity> {
    @Override
    public ResourceLocation getModelResource(RaidHomeBlockEntity blockEntity) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "geo/raid_crystal_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RaidHomeBlockEntity blockEntity) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/block/raid_crystal_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RaidHomeBlockEntity blockEntity) {
        return null;
    }
}
