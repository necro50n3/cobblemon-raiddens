package com.necro.raid.dens.neoforge.mixins.showdown;

import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownUnbundler;
import com.necro.raid.dens.neoforge.showdown.loader.NeoForgeShowdownLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GraalShowdownUnbundler.class)
public class GraalShowdownUnbundlerMixin {
    @Unique
    private boolean crd_loadedStatuses = false;

    @Inject(method = "attemptUnbundle", at = @At("TAIL"), remap = false)
    private void attemptUnbundleInject(CallbackInfo ci) {
        if (!this.crd_loadedStatuses) {
            new NeoForgeShowdownLoader().load();
            this.crd_loadedStatuses = true;
        }
    }
}
