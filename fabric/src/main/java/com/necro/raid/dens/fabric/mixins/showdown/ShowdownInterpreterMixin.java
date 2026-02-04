package com.necro.raid.dens.fabric.mixins.showdown;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.InstructionSet;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.necro.raid.dens.common.showdown.compat.ShowdownInterpreter171Impl;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function6;
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
    @Shadow(remap = false)
    private static Map<String, Function4<PokemonBattle, InstructionSet, BattleMessage, Iterator<BattleMessage>, InterpreterInstruction>> updateInstructionParser;

    @Final
    @Shadow(remap = false)
    private static Map<String, Function6<PokemonBattle, BattleActor, InstructionSet, BattleMessage, BattleMessage, Iterator<BattleMessage>, InterpreterInstruction>> splitInstructionParser;

    @Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
    private static void clinitInject(CallbackInfo ci) {
        ShowdownInterpreter171Impl.updateInstructionParser(updateInstructionParser);
        ShowdownInterpreter171Impl.splitInstructionParser(splitInstructionParser);
    }
}
