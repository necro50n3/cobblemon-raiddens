package com.necro.raid.dens.common.client.block;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.iris.RaidDensIrisCompat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class RaidDenBeamRenderer {
    public static final Set<BiConsumer<PoseStack, MultiBufferSource.BufferSource>> RENDER_QUEUE = new HashSet<>();
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/entity/raid_den_beam.png");
    public static final ResourceLocation GLOW_LOCATION = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/entity/raid_den_glow.png");

    private static final RenderType RAID_DEN_BEAM;
    private static final RenderType RAID_DEN_GLOW;

    public static void tick(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        RaidDenBeamRenderer.RENDER_QUEUE.forEach(consumer -> consumer.accept(poseStack, bufferSource));
        bufferSource.endBatch();
        RaidDenBeamRenderer.RENDER_QUEUE.clear();
    }

    public static void render(Vec3 pos, int height, int color) {
        RENDER_QUEUE.add((poseStack, multiBufferSource) -> renderBeam(poseStack, multiBufferSource, pos, height, color));
    }

    private static void renderBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 pos, int height, int color) {
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(1, Minecraft.getInstance().getMainRenderTarget().getDepthTextureId());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        poseStack.pushPose();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(pos.x - cameraPos.x, pos.y - cameraPos.y + 0.15, pos.z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot() + 180));

        RenderType renderType;
        if (ModCompat.IRIS.isLoaded() && RaidDensIrisCompat.isEnabled()) renderType = RenderType.entityTranslucentEmissive(BEAM_LOCATION, false);
        else renderType = RAID_DEN_BEAM;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();
        addVertices(vertexConsumer, pose, 0.5F, height, color);

        if (ModCompat.IRIS.isLoaded() && RaidDensIrisCompat.isEnabled()) renderType = RenderType.entityTranslucentEmissive(GLOW_LOCATION, false);
        else renderType = RAID_DEN_GLOW;
        vertexConsumer = bufferSource.getBuffer(renderType);
        pose = poseStack.last();
        addVertices(vertexConsumer, pose, 0.8F, 5.0F, color);

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableDepthTest();
    }

    private static void addVertices(VertexConsumer vertexConsumer, PoseStack.Pose pose, float radius, float height, int color) {
        vertexConsumer.addVertex(pose.pose(), -radius, 0, 0.0F).setUv(0, 1).setColor(color).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        vertexConsumer.addVertex(pose.pose(), radius, 0, 0.0F).setUv(1, 1).setColor(color).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        vertexConsumer.addVertex(pose.pose(), radius, height, 0.0F).setUv(1, 0).setColor(color).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        vertexConsumer.addVertex(pose.pose(), -radius, height, 0.0F).setUv(0, 0).setColor(color).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
    }

    static {
        RenderType.CompositeState beamState = RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_BEACON_BEAM_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(BEAM_LOCATION, false, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false);

        RAID_DEN_BEAM = RenderType.create(
            "raid_den_beam",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            beamState
        );

        RenderType.CompositeState glowState = RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_BEACON_BEAM_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(GLOW_LOCATION, false, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false);

        RAID_DEN_GLOW = RenderType.create(
            "raid_den_glow",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            glowState
        );
    }
}
