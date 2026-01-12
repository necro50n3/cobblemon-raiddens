package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.interpreter.instructions.SideEndInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.battle.RaidConditions;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SideEndInstruction.class)
public abstract class SideEndInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        BattlePokemon battlePokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return;

        String sideString = this.getMessage().argumentAt(0);
        if (sideString == null) return;
        char side = sideString.charAt(1);

        Effect effect = this.getMessage().effectAt(1);
        if (effect == null) return;
        int idx = effect.getRawData().lastIndexOf(" ");
        String sideCondition = (idx == -1 ? effect.getRawData() : effect.getRawData().substring(idx + " ".length())).toLowerCase();

        BattleContext.Type type;
        if (RaidConditions.SCREENS.contains(sideCondition)) type = BattleContext.Type.SCREEN;
        else if (RaidConditions.HAZARDS.contains(sideCondition)) type = BattleContext.Type.HAZARD;
        else if (RaidConditions.TAILWIND.contains(sideCondition)) type = BattleContext.Type.TAILWIND;
        else return;

        battle.dispatch(() -> {
            if (side == '1') {
                raid.updateBattleState(battle, battleState -> battleState.trainerSide.removeSideCondition(sideCondition));
                Component lang = LocalizationUtilsKt.battleLang(String.format("sideend.ally.%s", effect.getId()));
                raid.updateBattleContext(battle, b -> {
                    b.getSide1().getContextManager().remove(effect.getId(), type);
                    b.broadcastChatMessage(lang);
                });
            }
            else {
                raid.updateBattleState(battle, battleState -> battleState.bossSide.removeSideCondition(sideCondition));
                Component lang = LocalizationUtilsKt.battleLang(String.format("sideend.opponent.%s", effect.getId()));
                raid.updateBattleContext(battle, b -> {
                    b.getSide2().getContextManager().remove(effect.getId(), type);
                    b.broadcastChatMessage(lang);
                });
            }
            return DispatchResultKt.getGO();
        });
    }
}
