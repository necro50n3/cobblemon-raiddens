package com.necro.raid.dens.common.mixins.battlesync;

import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.FieldStartInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FieldStartInstruction.class)
public abstract class FieldStartInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getMessage();

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private void invokeInject(PokemonBattle battle, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        BattlePokemon battlePokemon = battle.getSide2().getActivePokemon().getFirst().getBattlePokemon();
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).crd_isRaidBoss()) return;

        Effect effect = this.getMessage().effectAt(0);
        if (effect == null) return;
        String field = effect.getId();

        int idx = effect.getRawData().lastIndexOf(" ");
        String effectType = idx == -1 ? effect.getRawData() : effect.getRawData().substring(idx + " ".length());
        BattleContext.Type type = BattleContext.Type.valueOf(effectType);

        BattlePokemon source = this.getMessage().battlePokemonFromOptional(battle, "of");

        battle.dispatchWaiting(1.5f, () -> {
            raid.updateBattleState(battle, battleState -> battleState.addTerrain(field));
            raid.updateBattleContext(battle, b -> {
                b.getContextManager().add(new BasicContext(field, b.getTurn(), type, null));
                b.broadcastChatMessage(LocalizationUtilsKt.battleLang(String.format("fieldstart.%s", field), source == null ? Component.literal("UNKNOWN") : source.getName()));
            });
            return Unit.INSTANCE;
        });
    }
}
