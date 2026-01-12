package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin implements IRaidBattle {
    @Shadow(remap = false)
    public abstract int getTurn();

    @Shadow(remap = false)
    public abstract void log(String string);

    @Shadow(remap = false)
    public abstract UUID getBattleId();

    @Unique
    private RaidInstance crd_raidInstance;

    @Unique
    private boolean crd_queueTera;

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

    @Inject(method = "writeShowdownAction", at = @At("HEAD"), remap = false, cancellable = true)
    private void triggerTerastallization(String[] messages, CallbackInfo ci) {
        if (this.getTurn() != 1 && !this.crd_queueTera) return;
        else if (!this.crd_isRaidBattle()) return;
        else if (!messages[0].startsWith(">p2")) return;
        else if (!this.crd_raidInstance.getRaidBoss().isTera()) return;

        if (!messages[0].startsWith(">p2 move")) {
            this.crd_queueTera = true;
            return;
        }

        this.log(String.join("\n", messages));
        String[] messageList = {messages[0] + " terastal"};
        ShowdownService.Companion.getService().send(this.getBattleId(), messageList);
        this.crd_queueTera = false;
        ci.cancel();
    }
}
