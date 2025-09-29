package com.necro.raid.dens.common.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.model.RaidCrystalBlockModel;
import com.necro.raid.dens.common.util.RaidUtils;
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

    @SuppressWarnings("unused")
    public RaidCrystalRenderer(BlockEntityRendererProvider.Context context) {
        super(new RaidCrystalBlockModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, RaidCrystalBlockEntity blockEntity, BakedGeoModel model, @Nullable RenderType renderType,
                               MultiBufferSource multiBufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float f, int i,
                               int j, int colour) {
        if (shouldRenderBeacon(blockEntity) && blockEntity.renderBeacon(blockEntity.getBlockState())
            && blockEntity.getLevel() != null && RaidUtils.hasSkyAccess(blockEntity.getLevel(), blockEntity.getBlockPos().above())) {
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

    private static boolean shouldRenderBeacon(RaidCrystalBlockEntity blockEntity) {
        return switch (blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TIER)) {
            case TIER_ONE -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_one;
            case TIER_TWO -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_two;
            case TIER_THREE -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_three;
            case TIER_FOUR -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_four;
            case TIER_FIVE -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_five;
            case TIER_SIX -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_six;
            case TIER_SEVEN -> CobblemonRaidDensClient.CLIENT_CONFIG.show_beam_tier_seven;
        };
    }
}
