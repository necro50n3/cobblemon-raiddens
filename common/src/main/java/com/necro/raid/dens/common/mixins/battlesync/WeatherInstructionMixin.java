package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.WeatherInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(WeatherInstruction.class)
public abstract class WeatherInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        BattlePokemon battlePokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return;

        if (this.getMessage().hasOptionalArgument("upkeep")) return;
        Effect effect = this.getMessage().effectAt(0);
        if (effect == null) return;
        String weather = effect.getId();

        battle.dispatch(() -> {
            if (!weather.equalsIgnoreCase("none")) {
                raid.updateBattleState(battle, battleState -> battleState.addWeather(weather));
                raid.updateBattleContext(battle, b -> {
                    b.getContextManager().add(new BasicContext(weather, b.getTurn(), BattleContext.Type.WEATHER, null));
                    b.broadcastChatMessage(LocalizationUtilsKt.battleLang(String.format("weather.%s.start", weather)));
                });
            }
            else {
                raid.updateBattleState(battle, RaidBattleState::removeWeather);
                raid.updateBattleContext(battle, b -> {
                    Collection<BattleContext> context = b.getContextManager().get(BattleContext.Type.WEATHER);
                    if (context == null || context.isEmpty()) return;
                    String oldWeather = context.stream().findFirst().get().getId();
                    b.getContextManager().clear(BattleContext.Type.WEATHER);
                    b.broadcastChatMessage(LocalizationUtilsKt.battleLang(String.format("weather.%s.end", oldWeather)));
                });
            }
            return DispatchResultKt.getGO();
        });
    }
}
