package com.necro.raid.dens.common.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.model.RaidCrystalBlockModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

@Environment(EnvType.CLIENT)
public class RaidCrystalRenderer extends GeoBlockRenderer<RaidCrystalBlockEntity> {
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    public RaidCrystalRenderer(BlockEntityRendererProvider.Context context) {
        super(new RaidCrystalBlockModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, RaidCrystalBlockEntity blockEntity, BakedGeoModel model, @Nullable RenderType renderType,
                               MultiBufferSource multiBufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float f, int i,
                               int j, int colour) {
        if (CobblemonRaidDens.CLIENT_CONFIG.show_beam && blockEntity.renderBeacon(blockEntity.getBlockState())
            && blockEntity.hasLevel() && blockEntity.getLevel().canSeeSky(blockEntity.getBlockPos().above())) {
            poseStack.pushPose();
            poseStack.scale(0.75f, 1.0f, 0.75f);
            poseStack.translate(-0.5, 0, -0.5);
            BeaconRenderer.renderBeaconBeam(
                poseStack, multiBufferSource, BEAM_LOCATION, f, 1.0f, blockEntity.getLevel().getGameTime(), 0, 1024,
                blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TYPE).getColor(), 0.2f, 0.25f
            );
            poseStack.popPose();
        }

        super.actuallyRender(poseStack, blockEntity, model, renderType, multiBufferSource, buffer, isReRender, f, i, j, colour);
    }

    @Override
    public boolean shouldRenderOffScreen(RaidCrystalBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(RaidCrystalBlockEntity blockEntity, Vec3 vec3) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(vec3.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }
}
