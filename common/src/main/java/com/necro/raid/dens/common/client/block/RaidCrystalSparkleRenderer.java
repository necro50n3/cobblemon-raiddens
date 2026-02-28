package com.necro.raid.dens.common.client.block;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.snowstorm.BedrockParticleOptions;
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository;
import com.cobblemon.mod.common.client.particle.ParticleStorm;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.blocks.block.RaidCrystalBlock;
import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidFeature;
import com.necro.raid.dens.common.data.raid.RaidType;
import kotlin.Unit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class RaidCrystalSparkleRenderer {
    public static void sparkle(RaidCrystalBlockEntity blockEntity) {
        BedrockParticleOptions effect = BedrockParticleOptionsRepository.INSTANCE.getEffect(ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_den_sparkle"));
        if (effect == null) return;
        MatrixWrapper wrapper = new MatrixWrapper();
        PoseStack matrix = new PoseStack();
        wrapper.updateMatrix(matrix.last().pose());
        wrapper.updatePosition(blockEntity.getBlockPos().getBottomCenter());
        RaidBoss boss = blockEntity.getRaidBoss();
        RaidFeature feature = boss == null ? RaidFeature.DEFAULT : boss.getFeature();
        RaidType type = blockEntity.getBlockState().getValue(RaidCrystalBlock.RAID_TYPE);

        new ParticleStorm(
            effect,
            wrapper,
            wrapper,
            (ClientLevel) blockEntity.getLevel(),
            () -> Vec3.ZERO,
            () -> blockEntity.isActive(blockEntity.getBlockState()) && !blockEntity.isRemoved(),
            () -> true,
            () -> null,
            () -> Unit.INSTANCE,
            () -> getParticleColor(feature, type),
            new MoLangRuntime(),
            null
        ).spawn();
    }

    private static Vector4f getParticleColor(RaidFeature feature, RaidType type) {
        if (feature == RaidFeature.DYNAMAX) return new Vector4f(1.0F, 0F, 0F, 0.5F);
        else if (feature == RaidFeature.TERA) return null;
        else return type.getVectorColor();
    }
}
