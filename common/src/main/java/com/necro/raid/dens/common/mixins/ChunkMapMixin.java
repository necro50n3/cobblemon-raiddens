package com.necro.raid.dens.common.mixins;

import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Final
    @Shadow
    ServerLevel level;

    @Inject(method = "isExistingChunkFull", at = @At("HEAD"), cancellable = true)
    private void isExistingChunkFullInject(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
        if (RaidUtils.isCustomDimension(this.level)) cir.setReturnValue(false);
    }
}
