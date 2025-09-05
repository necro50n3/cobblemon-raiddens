package com.necro.raid.dens.common.client.block;

import com.necro.raid.dens.common.blocks.entity.RaidHomeBlockEntity;
import com.necro.raid.dens.common.blocks.model.RaidHomeBlockModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

@Environment(EnvType.CLIENT)
public class RaidHomeRenderer extends GeoBlockRenderer<RaidHomeBlockEntity> {

    public RaidHomeRenderer(BlockEntityRendererProvider.Context context) {
        super(new RaidHomeBlockModel());
    }
}
