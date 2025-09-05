package com.necro.raid.dens.common.client.item;

import com.necro.raid.dens.common.items.item.RaidHomeBlockItem;
import com.necro.raid.dens.common.items.model.RaidHomeItemModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RaidHomeItemRenderer extends GeoItemRenderer<RaidHomeBlockItem> {
    public RaidHomeItemRenderer() {
        super(new RaidHomeItemModel());
    }
}