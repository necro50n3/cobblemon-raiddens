package com.necro.raid.dens.common.mixins.den;

import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Redirect(method = "setBlockState", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
    ))
    private void onPlaceInject(BlockState instance, Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        if (!RaidUtils.isRaidDimension(level)) instance.onPlace(level, blockPos, blockState, bl);
    }
}
