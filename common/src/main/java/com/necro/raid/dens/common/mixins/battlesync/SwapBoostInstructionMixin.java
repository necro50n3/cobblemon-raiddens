package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.SwapBoostInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwapBoostInstruction.class)
public abstract class SwapBoostInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();

        BattlePokemon battlePokemon = this.getMessage().battlePokemon(0, battle);
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        BattlePokemon target = this.getMessage().battlePokemon(1, battle);
        if (target == null) return;

        battle.dispatch(() -> {
            BattlePokemon copyFrom = ((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss() ? battlePokemon : target;
            copyFrom.getStatChanges().forEach((stat, stages) -> {
                raid.updateBattleState(battle, battleState -> battleState.bossSide.pokemon.setBoost(stat, stages));
                raid.updateBattleContext(battle, b -> {
                    BattlePokemon pokemon = b.getSide2().getActivePokemon().getFirst().getBattlePokemon();
                    if (pokemon == null) return;
                    BattleContext context = new BasicContext(stat.getShowdownId(), battle.getTurn(), BattleContext.Type.BOOST, null);
                    pokemon.getContextManager().add(context);
                });
            });
            return DispatchResultKt.getGO();
        });
    }
}
