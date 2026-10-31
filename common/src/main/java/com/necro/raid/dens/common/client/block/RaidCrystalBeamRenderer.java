package com.necro.raid.dens.common.client.block;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

public class RaidCrystalBeamRenderer {
    private static final List<Instance> RENDER_QUEUE = new ArrayList<>();

    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/entity/raid_den_beam.png");
    public static final ResourceLocation GLOW_LOCATION = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/entity/raid_den_glow.png");
    public static final ResourceLocation ADVENTURE_BEAM_LOCATION = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/entity/raid_adventure_beam.png");
    public static final ResourceLocation ADVENTURE_GLOW_LOCATION = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/entity/raid_adventure_glow.png");

    private static final RenderType RAID_DEN_BEAM;
    private static final RenderType RAID_DEN_GLOW;
    private static final RenderType RAID_ADVENTURE_BEAM;
    private static final RenderType RAID_ADVENTURE_GLOW;

    public static void tick(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource) {
        RENDER_QUEUE.sort(Comparator.comparingDouble(Instance::distanceSqr));

        Boolean lastAdventure = null;
        for (Instance instance : RENDER_QUEUE) {
            if (lastAdventure != null && lastAdventure != instance.isAdventure()) bufferSource.endBatch();
            instance.consumer().accept(poseStack, bufferSource);
            lastAdventure = instance.isAdventure();
        }
        bufferSource.endBatch();
        RENDER_QUEUE.clear();
    }

    public static void render(Vec3 pos, double distanceSqr, int height, int color, int alpha, boolean isAdventure) {
        RENDER_QUEUE.add(new Instance(-distanceSqr, isAdventure, (poseStack, multiBufferSource) -> renderBeam(poseStack, multiBufferSource, pos, height, color, alpha, isAdventure)));
    }

    public static void renderBeam(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 pos, int height, int color, int alpha, boolean isAdventure) {
        poseStack.pushPose();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(pos.x - cameraPos.x, pos.y - cameraPos.y + 0.15, pos.z - cameraPos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180F - camera.getYRot()));

        RenderType renderType;
        VertexConsumer vertexConsumer;

        if (ModCompat.IRIS.isLoaded() && RaidDensIrisCompat.isEnabled()) {
            // Black does not render as light beams using shaders. Change to a gray.
            if (color == 657930) color = 6250335;
            renderType = RenderType.entityTranslucentEmissive(isAdventure ? ADVENTURE_BEAM_LOCATION : BEAM_LOCATION, false);
        }
        else renderType = isAdventure ? RAID_ADVENTURE_BEAM : RAID_DEN_BEAM;

        vertexConsumer = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();
        addVertices(vertexConsumer, pose, 0.5F, height, color, alpha);

        if (ModCompat.IRIS.isLoaded() && RaidDensIrisCompat.isEnabled()) renderType = RenderType.entityTranslucentEmissive(isAdventure ? ADVENTURE_GLOW_LOCATION : GLOW_LOCATION, false);
        else renderType = isAdventure ? RAID_ADVENTURE_GLOW : RAID_DEN_GLOW;
        vertexConsumer = bufferSource.getBuffer(renderType);
        addVertices(vertexConsumer, pose, 1.0F, 5.0F, color, alpha);

        poseStack.popPose();
    }

    private static void addVertices(VertexConsumer vertexConsumer, PoseStack.Pose pose, float radius, float height, int color, int alpha) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        vertexConsumer.addVertex(pose.pose(), -radius, 0, 0.0F).setUv(0, 1).setColor(r, g, b, alpha).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        vertexConsumer.addVertex(pose.pose(), radius, 0, 0.0F).setUv(1, 1).setColor(r, g, b, alpha).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        vertexConsumer.addVertex(pose.pose(), radius, height, 0.0F).setUv(1, 0).setColor(r, g, b, alpha).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
        vertexConsumer.addVertex(pose.pose(), -radius, height, 0.0F).setUv(0, 0).setColor(r, g, b, alpha).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(pose, 0, 1, 0);
    }

    private record Instance(double distanceSqr, boolean isAdventure, BiConsumer<PoseStack, MultiBufferSource.BufferSource> consumer) {}

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

        RenderType.CompositeState adventureBeamState = RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_BEACON_BEAM_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ADVENTURE_BEAM_LOCATION, false, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false);

        RAID_ADVENTURE_BEAM = RenderType.create(
            "raid_adventure_beam",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            adventureBeamState
        );

        RenderType.CompositeState adventureGlowState = RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_BEACON_BEAM_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(ADVENTURE_GLOW_LOCATION, false, false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setCullState(RenderStateShard.NO_CULL)
            .createCompositeState(false);

        RAID_ADVENTURE_GLOW = RenderType.create(
            "raid_adventure_glow",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            adventureGlowState
        );
    }
}
