package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.RaidSession;
import com.necro.raid.dens.common.raids.scripts.RaidTriggerType;
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

    @Shadow
    public abstract boolean getEnded();

    @Shadow
    public abstract void stop();

    @Unique
    private RaidSession crd_raidSession;

    @Override
    public boolean crd_isRaid() {
        return this.crd_raidSession != null;
    }

    @Override
    public RaidSession crd_getRaidSession() {
        return this.crd_raidSession;
    }

    @Override
    public void crd_setRaidSession(RaidSession raid) {
        this.crd_raidSession = raid;
    }

    @Inject(method = "turn", at = @At("RETURN"), remap = false)
    private void turnInject(int newTurnNumber, CallbackInfo ci) {
        if (!this.crd_isRaid()) return;
        else if (this.crd_raidSession.getActive() == null) return;
        this.crd_raidSession.getActive().runScripts(RaidTriggerType.TURN, (PokemonBattle) (Object) this, () -> newTurnNumber);
    }

    @Inject(method = "checkFlee", at = @At("HEAD"), remap = false, cancellable = true)
    private void checkFleeInject(CallbackInfo ci) {
        if (!this.crd_isRaid()) return;
        RaidSession session = this.crd_getRaidSession();
        RaidInstance raid = session.getActive();
        if (session.isFinished() || raid == null || raid.isFinished()) {
            if (!this.getEnded()) this.stop();
            ci.cancel();
        }
    }
}
