package com.necro.raid.dens.common.client.item;

import com.necro.raid.dens.common.items.item.RaidCrystalBlockItem;
import com.necro.raid.dens.common.items.model.RaidCrystalItemModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RaidCrystalItemRenderer extends GeoItemRenderer<RaidCrystalBlockItem> {
    public RaidCrystalItemRenderer() { super(new RaidCrystalItemModel()); }
}