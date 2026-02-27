package com.necro.raid.dens.common.client.block;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions;
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository;
import com.cobblemon.mod.common.client.particle.ParticleStorm;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.blocks.model.RaidCrystalBlockModel;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidType;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class RaidCrystalRenderer extends GeoBlockRenderer<RaidCrystalBlockEntity> {
    public static final ResourceLocation LEGACY_BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    @SuppressWarnings("unused")
    public RaidCrystalRenderer(BlockEntityRendererProvider.Context context) {
        super(new RaidCrystalBlockModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, RaidCrystalBlockEntity blockEntity, BakedGeoModel model, @Nullable RenderType renderType,
                               MultiBufferSource multiBufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float f, int i,
                               int j, int colour) {
        Level level = blockEntity.getLevel();
        if (level != null && checkConfig(blockEntity) && blockEntity.canGenerateBoss(blockEntity.getBlockState())) {
            if (CobblemonRaidDensClient.CLIENT_CONFIG.show_legacy_beacon) this.renderLegacyBeacon(blockEntity, level, poseStack, multiBufferSource, f);
            else this.renderBeam(blockEntity, level);
        }

        super.actuallyRender(poseStack, blockEntity, model, renderType, multiBufferSource, buffer, isReRender, f, i, j, colour);
    }

    private void renderLegacyBeacon(RaidCrystalBlockEntity blockEntity, Level level, PoseStack poseStack, MultiBufferSource multiBufferSource, float f) {
        if (!RaidUtils.hasSkyAccess(level, blockEntity.getBlockPos())) return;
        poseStack.pushPose();
        poseStack.scale(0.75f, 1.0f, 0.75f);
        poseStack.translate(-0.5, 0, -0.5);
        BeaconRenderer.renderBeaconBeam(
            poseStack, multiBufferSource, LEGACY_BEAM_LOCATION, f, 1.0f, level.getGameTime(), 0, 1024,
            blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TYPE).getColor(), 0.2f, 0.25f
        );
        poseStack.popPose();
    }

    private void renderBeam(RaidCrystalBlockEntity blockEntity, Level level) {
        int tick = blockEntity.getParticleTick();
        if (tick % 20 == 1) this.renderSparkle(blockEntity, level, blockEntity.getBlockState(), blockEntity.getBlockPos());

        assert Minecraft.getInstance().player != null;
        BlockPos playerPos = Minecraft.getInstance().player.blockPosition();

        if (blockEntity.getBeamHeight() > 4) {
            RaidDenBeamRenderer.render(
                blockEntity.getBlockPos().getBottomCenter(),
                playerPos.distSqr(blockEntity.getBlockPos()),
                blockEntity.getBeamHeight(),
                blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TYPE).getColor()
            );
        }
    }

    private void renderSparkle(RaidCrystalBlockEntity blockEntity, Level level, BlockState blockState, BlockPos blockPos) {
        BedrockParticleOptions effect = BedrockParticleOptionsRepository.INSTANCE.getEffect(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_sparkle"));
        if (effect == null) return;
        MatrixWrapper wrapper = new MatrixWrapper();
        PoseStack matrix = new PoseStack();
        wrapper.updateMatrix(matrix.last().pose());
        wrapper.updatePosition(blockPos.getBottomCenter());
        RaidBoss boss = blockEntity.getRaidBoss();
        RaidFeature feature = boss == null ? RaidFeature.DEFAULT : boss.getFeature();
        RaidType type = blockState.getValue(RaidCrystalBlock.RAID_TYPE);
        new ParticleStorm(
            effect,
            wrapper,
            wrapper,
            (ClientLevel) level,
            () -> Vec3.ZERO,
            () -> blockEntity.isActive(blockState) && !blockEntity.isRemoved(),
            () -> true,
            () -> null,
            () -> Unit.INSTANCE,
            () -> this.getParticleColor(feature, type),
            new MoLangRuntime(),
            null
        ).spawn();
    }

    private Vector4f getParticleColor(RaidFeature feature, RaidType type) {
        if (feature == RaidFeature.DYNAMAX) return new Vector4f(1.0F, 0F, 0F, 0.5F);
        else if (feature == RaidFeature.TERA) return null;
        else return type.getVectorColor();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull RaidCrystalBlockEntity blockEntity) {
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

    private static boolean checkConfig(RaidCrystalBlockEntity blockEntity) {
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
