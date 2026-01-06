package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.necro.raid.dens.common.CobblemonRaidDens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GraalShowdownService.class)
public class GraalShowdownServiceMixin {
    @Inject(method = "sendFromShowdown", at = @At("HEAD"), remap = false)
    private void debugFromShowdown(String battleId, String message, CallbackInfo ci) {
        CobblemonRaidDens.LOGGER.info(message);
    }
}
