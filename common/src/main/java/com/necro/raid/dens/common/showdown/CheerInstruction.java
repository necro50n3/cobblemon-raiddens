package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record CheerInstruction(BattleMessage message) implements InterpreterInstruction {
    @Override
    public void invoke(@NotNull PokemonBattle battle) {
        battle.dispatchGo(() -> {
            String cheer = message.argumentAt(0);
            String origin = message.argumentAt(1);
            if (cheer == null || origin == null) return Unit.INSTANCE;

            battle.broadcastChatMessage(
                Component.translatable("battle.cobblemonraiddens.cheer." + cheer, origin)
            );

            return Unit.INSTANCE;
        });
    }
}
