package com.necro.raid.dens.common.mixins.client;

import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.util.math.DoubleRange;
import com.cobblemon.mod.common.util.math.SimpleMathExtensionsKt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.CobblemonRaidDensClient;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonRenderer.class)
public abstract class PokemonRendererMixin extends EntityRenderer<PokemonEntity> {
    @Unique
    private static final ResourceLocation crd_TEXTURE = ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "textures/gui/raid/health_bar.png");

    protected PokemonRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Unique
    private boolean crd_shouldRenderHpBar(PokemonEntity entity) {
        assert Minecraft.getInstance().player != null;
        return CobblemonRaidDensClient.CLIENT_CONFIG.enable_health_bars
            && !Minecraft.getInstance().player.getUUID().equals(entity.getOwnerUUID())
            && ((IRaidAccessor) entity).crd_shouldRenderHpBar()
            && !((IRaidAccessor) entity).crd_isRaidBoss();
    }

    @Unique
    private void crd_renderHpBar(PokemonEntity entity, PoseStack poseStack, MultiBufferSource buffer) {
        double d = this.entityRenderDispatcher.distanceToSqr(entity);
        if (d > 4096.0) return;

        double scale = Math.min(1.5, Math.max(0.65, SimpleMathExtensionsKt.remap(this.entityRenderDispatcher.distanceToSqr(entity), new DoubleRange(-16.0, 96.0), new DoubleRange(0.0, 1.0))));
        double sizeScale = Mth.lerp(SimpleMathExtensionsKt.remap(scale, new DoubleRange(0.65, 1.5), new DoubleRange(0.0,1.0)), 0.5, 1.0);
        double offsetScale = Mth.lerp(SimpleMathExtensionsKt.remap(scale, new DoubleRange(0.65, 1.5), new DoubleRange(0.0,1.0)), 0.0,1.0);
        double entityHeight = entity.getBoundingBox().getYsize() + 0.5F;
        poseStack.pushPose();
        poseStack.translate(0.0, entityHeight, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.translate(0.0, 0.0 + (offsetScale / 2), 0.0);
        poseStack.scale((float) (0.0125 * sizeScale), (float) (-0.0125 * sizeScale), (float) (1 * sizeScale));

        float currentHeath = entity.getPokemon().getCurrentHealth();
        float maxHealth = entity.getPokemon().getMaxHealth();
        float healthRatio = currentHeath / maxHealth;
        MutableComponent label = Component.literal(String.format("%d%%", Math.round(healthRatio * 100)));

        float width = 102F;
        float height = 12F;

        float x = width / 2F;
        float y0 = -height / 2F - 12F;
        float y1 = height / 2F - 12F;

        float filledWidth = width * healthRatio;
        float filledX = -x + filledWidth;

        Matrix4f pose = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(crd_TEXTURE));

        vertexConsumer.addVertex(pose, -x, y0, -0.003F).setUv(0F, 0F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        vertexConsumer.addVertex(pose, -x, y1, -0.003F).setUv(0F, 0.5F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        vertexConsumer.addVertex(pose, x, y1, -0.003F).setUv(1F, 0.5F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        vertexConsumer.addVertex(pose, x, y0, -0.003F).setUv(1F, 0F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);

        vertexConsumer.addVertex(pose, -x, y0, -0.002F).setUv(0F, 0.5F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        vertexConsumer.addVertex(pose, -x, y1, -0.002F).setUv(0F, 1F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        vertexConsumer.addVertex(pose, filledX, y1, -0.002F).setUv(healthRatio, 1F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);
        vertexConsumer.addVertex(pose, filledX, y0, -0.002F).setUv(healthRatio, 0.5F).setColor(16777215).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(0, 1, 0);

        float h = -this.getFont().width(label) / 2F;
        int packedLight = LightTexture.pack(15, 15);

        this.getFont().drawInBatch(label, h, -16F, -1, false, pose, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
        Matrix4f matrix = new Matrix4f(pose).translate(1F, 1F, -0.001F);
        this.getFont().drawInBatch(label, h, -16F, 5592405, false, matrix, buffer, Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }

    @Inject(method = "render(Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), remap = false)
    private void renderInject(PokemonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (this.crd_shouldRenderHpBar(entity)) this.crd_renderHpBar(entity, poseStack, buffer);
    }
}
