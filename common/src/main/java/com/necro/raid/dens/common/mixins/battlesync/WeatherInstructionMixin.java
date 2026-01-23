package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.WeatherInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mixin(WeatherInstruction.class)
public abstract class WeatherInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false, cancellable = true)
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

        BattlePokemon source = this.getMessage().battlePokemonFromOptional(battle, "of");
        if (source != null) ShowdownInterpreter.INSTANCE.broadcastOptionalAbility(battle, this.getMessage().effect("from"), source);

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

        battle.dispatchWaiting(1.5f, () -> {
            Component lang;
            if (this.getMessage().hasOptionalArgument("upkeep")) {
                lang = LocalizationUtilsKt.battleLang(String.format("weather.%s.upkeep", weather));
            }
            else if (!weather.equals("none")) {
                battle.getContextManager().add(ShowdownInterpreter.INSTANCE.getContextFromAction(this.getMessage(), BattleContext.Type.WEATHER, battle));
                lang = LocalizationUtilsKt.battleLang(String.format("weather.%s.start", weather));
            }
            else {
                Collection<BattleContext> context = battle.getContextManager().get(BattleContext.Type.WEATHER);
                if (context == null || context.isEmpty()) return Unit.INSTANCE;
                String oldWeather = context.stream().findFirst().get().getId();
                battle.getContextManager().clear(BattleContext.Type.WEATHER);
                lang = LocalizationUtilsKt.battleLang(String.format("weather.%s.end", oldWeather));
            }

            battle.broadcastChatMessage(lang);
            return Unit.INSTANCE;
        });

        ci.cancel();
    }
}
