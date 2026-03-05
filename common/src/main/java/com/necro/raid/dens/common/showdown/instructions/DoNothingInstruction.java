package com.necro.raid.dens.common.showdown.instructions;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import org.jetbrains.annotations.NotNull;

public class DoNothingInstruction implements InterpreterInstruction {
    @Override
    public void invoke(@NotNull PokemonBattle pokemonBattle) {}
}
