package com.necro.raid.dens.fabric.mixins.showdown;

import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler;
import com.necro.raid.dens.fabric.showdown.loader.FabricShowdownLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GraalShowdownUnbundler.class)
public class GraalShowdownUnbundlerMixin {
    @Unique
    private boolean loadedStatuses = false;

    @Inject(method = "attemptUnbundle", at = @At("TAIL"), remap = false)
    private void attemptUnbundleInject(CallbackInfo ci) {
        if (!this.loadedStatuses) {
            new FabricShowdownLoader().load();
            this.loadedStatuses = true;
        }
    }
}
