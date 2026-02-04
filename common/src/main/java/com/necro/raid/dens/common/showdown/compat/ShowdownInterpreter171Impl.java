package com.necro.raid.dens.common.showdown.compat;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.dispatch.InstructionSet;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.necro.raid.dens.common.showdown.instructions.*;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function6;

import java.util.Iterator;
import java.util.Map;

public class ShowdownInterpreter171Impl {
    public static void updateInstructionParser(Map<String, Function4<PokemonBattle, InstructionSet, BattleMessage, Iterator<BattleMessage>, InterpreterInstruction>> parser) {
        parser.put("cheer", (battle, instruction, message, messageIterator) ->
            new CheerInstruction(message)
        );
        parser.put("cheerboost", (battle, instruction, message, messageIterator) ->
            new CheerBoostInstruction(battle, message)
        );
        parser.put("clearboss", (battle, instruction, message, messageIterator) ->
            new ResetBossInstruction(battle, message)
        );
        parser.put("clearplayer", (battle, instruction, message, messageIterator) ->
            new ResetPlayerInstruction(battle, message)
        );
        parser.put("raidenergy", (battle, instruction, message, messageIterator) ->
            new RaidEnergyInstruction(battle, message)
        );
        parser.put("playerjoin", (battle, instruction, message, messageIterator) ->
            new PlayerJoinInstruction(battle, message)
        );
        parser.put("shieldadd", (battle, instruction, message, messageIterator) ->
            new ShieldAddInstruction(battle, message)
        );
        parser.put("shieldremove", (battle, instruction, message, messageIterator) ->
            new ShieldRemoveInstruction(battle, message)
        );
        parser.put("-raidboost", (battle, instruction, message, messageIterator) ->
            new RaidBoostInstruction(battle, message, true)
        );
        parser.put("-raidunboost", (battle, instruction, message, messageIterator) ->
            new RaidBoostInstruction(battle, message, false)
        );
    }

    public static void splitInstructionParser(Map<String, Function6<PokemonBattle, BattleActor, InstructionSet, BattleMessage, BattleMessage, Iterator<BattleMessage>, InterpreterInstruction>> parser) {
        parser.put("-raiddamage", (battle, actor, instruction, publicMessage, privateMessage, messageIterator) ->
            new RaidDamageInstruction(instruction, actor, publicMessage, privateMessage)
        );
        parser.put("-raidheal", (battle, actor, instruction, publicMessage, privateMessage, messageIterator) ->
            new RaidHealInstruction(actor, publicMessage, privateMessage)
        );
    }
}
