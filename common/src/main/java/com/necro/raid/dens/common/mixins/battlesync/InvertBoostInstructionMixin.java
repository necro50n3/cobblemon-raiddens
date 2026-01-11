package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.InvertBoostInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(InvertBoostInstruction.class)
public abstract class InvertBoostInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        BattlePokemon battlePokemon = this.getMessage().battlePokemon(0, battle);
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return;

        battle.dispatch(() -> {
            raid.updateBattleState(battle, battleState -> battleState.bossSide.pokemon.invertBoosts());
            raid.updateBattleContext(battle, b -> {
                BattlePokemon pokemon = b.getSide2().getActivePokemon().getFirst().getBattlePokemon();
                if (pokemon == null) return;

                Collection<BattleContext> newBoosts = pokemon.getContextManager().get(BattleContext.Type.UNBOOST);
                if (newBoosts == null) newBoosts = new ArrayList<>();
                Collection<BattleContext> newUnboosts = pokemon.getContextManager().get(BattleContext.Type.BOOST);
                if (newUnboosts == null) newUnboosts = new ArrayList<>();

                newBoosts = newBoosts.stream().map(ctx -> new BasicContext(ctx.getId(), ctx.getTurn(), BattleContext.Type.BOOST, null)).collect(Collectors.toList());
                newUnboosts = newUnboosts.stream().map(ctx -> new BasicContext(ctx.getId(), ctx.getTurn(), BattleContext.Type.UNBOOST, null)).collect(Collectors.toList());
                newBoosts.forEach(context -> pokemon.getContextManager().add(context));
                newUnboosts.forEach(context -> pokemon.getContextManager().add(context));
            });
            return DispatchResultKt.getGO();
        });
    }
}
