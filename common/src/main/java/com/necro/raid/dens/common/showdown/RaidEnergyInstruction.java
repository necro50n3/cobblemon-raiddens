package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record RaidEnergyInstruction(PokemonBattle battle, BattleMessage message) implements InterpreterInstruction {
    @Override
    public void invoke(@NotNull PokemonBattle battle) {
        battle.dispatchGo(() -> {
            BattlePokemon origin = message.pokemonByUuid(0, battle);
            if (origin == null || origin.getEntity() == null) return Unit.INSTANCE;

            battle.broadcastChatMessage(
                Component.translatable("battle.cobblemonraiddens.raid_energy", origin.getName())
            );

            origin.getEntity().cry();

            return Unit.INSTANCE;
        });
    }
}
