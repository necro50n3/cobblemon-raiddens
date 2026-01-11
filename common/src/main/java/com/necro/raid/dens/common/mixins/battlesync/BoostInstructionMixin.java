package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.BoostInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoostInstruction.class)
public abstract class BoostInstructionMixin {
    @Shadow(remap = false)
    public abstract BattlePokemon getPokemon();

    @Shadow(remap = false)
    public abstract int getStages();

    @Shadow(remap = false)
    public abstract String getStatKey();

    @Shadow
    public abstract boolean isBoost();

    @Inject(method = "postActionEffect", at = @At("HEAD"), remap = false)
    private void postActionEffectInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        BattlePokemon battlePokemon = this.getPokemon();
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return;

        Stat stat = Stats.Companion.getStat(this.getStatKey());
        battle.dispatch(() -> {
            int stages = this.getStages();
            raid.updateBattleState(battle, battleState -> battleState.bossSide.pokemon.boost(stat, this.isBoost() ? stages : -stages));
            raid.updateBattleContext(battle, b -> {
                BattlePokemon pokemon = b.getSide2().getActivePokemon().getFirst().getBattlePokemon();
                if (pokemon == null) return;
                BattleContext context = new BasicContext(this.getStatKey(), battle.getTurn(), this.isBoost() ? BattleContext.Type.BOOST : BattleContext.Type.UNBOOST, null);
                for (int i = 0; i < stages; i++) pokemon.getContextManager().add(context);
            });
            return DispatchResultKt.getGO();
        });
    }
}
