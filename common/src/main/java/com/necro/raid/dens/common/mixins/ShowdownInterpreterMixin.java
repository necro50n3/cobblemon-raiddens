package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.InstructionSet;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.necro.raid.dens.common.showdown.CheerInstruction;
import com.necro.raid.dens.common.showdown.ResetBossInstruction;
import com.necro.raid.dens.common.showdown.ResetPlayerInstruction;
import kotlin.jvm.functions.Function4;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

@Mixin(ShowdownInterpreter.class)
public class ShowdownInterpreterMixin {
    @Final
    @Shadow
    private static Map<String, Function4<PokemonBattle, InstructionSet, BattleMessage, Iterator<BattleMessage>, InterpreterInstruction>> updateInstructionParser;

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void clinitInject(CallbackInfo ci) {
        updateInstructionParser.put("cheer", (battle, instruction, message, messageIterator) ->
            new CheerInstruction(message)
        );
        updateInstructionParser.put("clearboss", (battle, instruction, message, messageIterator) ->
            new ResetBossInstruction(battle, message)
        );
        updateInstructionParser.put("clearplayer", (battle, instruction, message, messageIterator) ->
            new ResetPlayerInstruction(battle, message)
        );
    }
}
