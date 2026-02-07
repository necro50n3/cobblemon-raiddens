package com.necro.raid.dens.neoforge.mixins.den;

import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public class LevelMixin {
    @ModifyConstant(method = "markAndNotifyBlock", constant = @Constant(intValue = 16))
    private int updateConstant(int original) {
        return RaidUtils.isRaidDimension((Level) (Object) this) ? -1 : original;
    }

    @Redirect(method = "markAndNotifyBlock", at = @At(
        value = "INVOKE",
        target  = "Lnet/minecraft/world/level/Level;blockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V"
    ))
    private void blockUpdatedInject(Level level, BlockPos blockPos, Block block) {
        if (!RaidUtils.isRaidDimension(level)) level.blockUpdated(blockPos, block);
    }
}
