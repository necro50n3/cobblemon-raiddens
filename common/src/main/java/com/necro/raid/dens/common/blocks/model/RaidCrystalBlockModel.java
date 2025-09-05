package com.necro.raid.dens.common.blocks.model;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RaidCrystalBlockModel extends GeoModel<RaidCrystalBlockEntity> {
    @Override
    public ResourceLocation getModelResource(RaidCrystalBlockEntity blockEntity) {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "geo/raid_crystal_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RaidCrystalBlockEntity blockEntity) {
        if (!blockEntity.getBlockState().getValue(RaidCrystalBlock.ACTIVE)) return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/block/raid_crystal_block.png");
        String colour = blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TYPE).getSerializedName();
        if (colour.equals("none")) return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/block/raid_crystal_block.png");
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, String.format("textures/block/raid_crystal_block_%s.png", colour));
    }

    @Override
    public ResourceLocation getAnimationResource(RaidCrystalBlockEntity blockEntity) {
        return null;
    }
}
