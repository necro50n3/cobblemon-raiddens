package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.interpreter.instructions.DamageInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidHelper;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageInstruction.class)
public abstract class ActionEffectInstructionMixin {
    @Final
    @Shadow(remap = false)
    private BattleMessage publicMessage;

    @Final
    @Shadow(remap = false)
    private BattleMessage privateMessage;

    @Final
    @Shadow(remap = false)
    private BattleActor actor;

    @Inject(method = "postActionEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private void postActionEffectInject(PokemonBattle battle, CallbackInfo ci) {
        BattlePokemon battlePokemon = publicMessage.battlePokemon(0, actor.battle);
        if (battlePokemon == null) return;
        else if (battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).isRaidBoss()) return;
        else if (!RaidHelper.ACTIVE_RAIDS.containsKey(((IRaidAccessor) battlePokemon.getEntity()).getRaidId())) return;

        String newHealth = this.privateMessage.argumentAt(1).split(" ")[0];
        RaidInstance raidInstance = RaidHelper.ACTIVE_RAIDS.get(((IRaidAccessor) battlePokemon.getEntity()).getRaidId());
        if (newHealth.equals("0")) {
            battlePokemon.getEffectedPokemon().setCurrentHealth(0);
            raidInstance.queueStopRaid();
        }
        else {
            float remainingHealth = Float.parseFloat(newHealth.split("/")[0]);
            battle.getPlayers().forEach(player -> raidInstance.syncHealth(player, remainingHealth));
            battlePokemon.getEffectedPokemon().setCurrentHealth((int) raidInstance.getRemainingHealth());
        }
        ci.cancel();
    }
}
