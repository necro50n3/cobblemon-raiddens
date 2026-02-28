package com.necro.raid.dens.neoforge.client.block;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import com.necro.raid.dens.common.client.block.RaidCrystalRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class RaidCrystalRendererNeoForge extends RaidCrystalRenderer {
    public RaidCrystalRendererNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(RaidCrystalBlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getBlockPos();
        if(!blockEntity.isActive(blockEntity.getBlockState())) return new AABB(blockPos);
        return new AABB(
            blockPos.getX(), blockPos.getY(), blockPos.getZ(),
            blockPos.getX() + 1, blockPos.getY() + 500, blockPos.getZ()
        );
    }
}
