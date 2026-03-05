package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin implements IRaidBattle {
    @Shadow(remap = false)
    public abstract int getTurn();

    @Shadow(remap = false)
    public abstract void log(String string);

    @Shadow
    public abstract boolean getEnded();

    @Shadow
    public abstract void stop();

    @Unique
    private RaidInstance crd_raidInstance;

    @Override
    public boolean crd_isRaidBattle() {
        return this.crd_raidInstance != null;
    }

    @Override
    public RaidInstance crd_getRaidBattle() {
        return this.crd_raidInstance;
    }

    @Override
    public void crd_setRaidBattle(RaidInstance raidInstance) {
        this.crd_raidInstance = raidInstance;
    }

    @Inject(method = "turn", at = @At("RETURN"), remap = false)
    private void turnInject(int newTurnNumber, CallbackInfo ci) {
        if (!this.crd_isRaidBattle()) return;
        this.crd_raidInstance.runScriptByTurn(newTurnNumber, (PokemonBattle) (Object) this);
    }

    @Inject(method = "checkFlee", at = @At("HEAD"), remap = false, cancellable = true)
    private void checkFleeInject(CallbackInfo ci) {
        if (this.crd_isRaidBattle() && this.crd_getRaidBattle().isFinished()) {
            if (!this.getEnded()) this.stop();
            ci.cancel();
        }
    }
}
