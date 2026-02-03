package com.necro.raid.dens.common.mixins.den;

import com.llamalad7.mixinextras.sugar.Local;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
    @Shadow
    protected abstract boolean hasFlammableNeighbours(LevelReader levelReader, BlockPos blockPos);

    @Redirect(
        method = "randomTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/LavaFluid;hasFlammableNeighbours(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean hasFlammableNeighboursInject(LavaFluid instance, LevelReader levelReader, BlockPos blockPos, @Local(argsOnly = true) Level level) {
        if (RaidUtils.isRaidDimension(level)) return false;
        return this.hasFlammableNeighbours(levelReader, blockPos);
    }
}
