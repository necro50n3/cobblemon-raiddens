package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.necro.raid.dens.common.showdown.instructions.*;

public class RaidDensShowdownRegistry {
    public static void registerInstructions() {
        ShowdownInterpreter.registerUpdateInstructionParser("cheer", (battle, instruction, message, messageIterator) ->
            new CheerInstruction(message)
        );

        ShowdownInterpreter.registerUpdateInstructionParser("cheerboost", (battle, instruction, message, messageIterator) ->
            new CheerBoostInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("clearboss", (battle, instruction, message, messageIterator) ->
            new ResetBossInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("clearplayer", (battle, instruction, message, messageIterator) ->
            new ResetPlayerInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("raidenergy", (battle, instruction, message, messageIterator) ->
            new RaidEnergyInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("playerjoin", (battle, instruction, message, messageIterator) ->
            new PlayerJoinInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("shieldadd", (battle, instruction, message, messageIterator) ->
            new ShieldAddInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("shieldremove", (battle, instruction, message, messageIterator) ->
            new ShieldRemoveInstruction(battle, message)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("-raidboost", (battle, instruction, message, messageIterator) ->
            new RaidBoostInstruction(battle, message, true)
        );
        ShowdownInterpreter.registerUpdateInstructionParser("-raidunboost", (battle, instruction, message, messageIterator) ->
            new RaidBoostInstruction(battle, message, false)
        );

        ShowdownInterpreter.registerSplitInstructionParser("-raiddamage", (battle, actor, instruction, publicMessage, privateMessage, messageIterator) ->
            new RaidDamageInstruction(instruction, actor, publicMessage, privateMessage)
        );
        ShowdownInterpreter.registerSplitInstructionParser("-raidheal", (battle, actor, instruction, publicMessage, privateMessage, messageIterator) ->
            new RaidHealInstruction(actor, publicMessage, privateMessage)
        );
    }
}
