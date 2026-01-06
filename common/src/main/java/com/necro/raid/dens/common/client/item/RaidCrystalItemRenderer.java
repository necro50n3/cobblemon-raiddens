package com.necro.raid.dens.common.client.item;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.components.ModComponents;
import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import com.necro.raid.dens.common.items.model.RaidCrystalItemModel;
import com.necro.raid.dens.common.data.raid.RaidType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RaidCrystalItemRenderer extends GeoItemRenderer<RaidCrystalBlockItem> {
    public RaidCrystalItemRenderer() { super(new RaidCrystalItemModel()); }

    @Override
    public ResourceLocation getTextureLocation(RaidCrystalBlockItem item) {
        RaidType type = this.getCurrentItemStack().get(ModComponents.TYPE_COMPONENT.value());
        if (type == null || type == RaidType.NONE) return super.getTextureLocation(item);
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/block/raid_crystal_block_" + type.getSerializedName() + ".png");
    }
}