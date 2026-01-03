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
    private RaidInstance raidInstance;

    @Unique
    private boolean queueTera;

    @Override
    public boolean isRaidBattle() {
        return this.raidInstance != null;
    }

    @Override
    public RaidInstance getRaidBattle() {
        return this.raidInstance;
    }

    @Override
    public void setRaidBattle(RaidInstance raidInstance) {
        this.raidInstance = raidInstance;
    }

    @Inject(method = "turn", at = @At("RETURN"), remap = false)
    private void turnInject(int newTurnNumber, CallbackInfo ci) {
        if (!this.isRaidBattle()) return;
        this.raidInstance.runScriptByTurn((PokemonBattle) (Object) this, newTurnNumber);
    }

    @Inject(method = "writeShowdownAction", at = @At("HEAD"), remap = false, cancellable = true)
    private void triggerTerastallization(String[] messages, CallbackInfo ci) {
        if (this.getTurn() != 1 && !this.queueTera) return;
        else if (!this.isRaidBattle()) return;
        else if (!messages[0].startsWith(">p2")) return;
        else if (!this.raidInstance.getRaidBoss().isTera()) return;

        if (!messages[0].startsWith(">p2 move")) {
            this.queueTera = true;
            return;
        }

        this.log(String.join("\n", messages));
        String[] messageList = {messages[0] + " terastal"};
        ShowdownService.Companion.getService().send(this.getBattleId(), messageList);
        this.queueTera = false;
        ci.cancel();
    }
}
