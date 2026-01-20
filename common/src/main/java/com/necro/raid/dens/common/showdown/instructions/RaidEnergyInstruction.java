package com.necro.raid.dens.common.showdown.instructions;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record RaidEnergyInstruction(PokemonBattle battle, BattleMessage message) implements InterpreterInstruction {
    @Override
    public void invoke(@NotNull PokemonBattle battle) {
        battle.dispatchGo(() -> {
            BattlePokemon origin = this.message.pokemonByUuid(0, battle);
            if (origin == null || origin.getEntity() == null) return Unit.INSTANCE;

            boolean broadcast = Boolean.parseBoolean(this.message.argumentAt(1));

            battle.broadcastChatMessage(Component.translatable("battle.cobblemonraiddens.raid_energy", origin.getName()));
            if (broadcast && ((IRaidBattle) battle).crd_isRaidBattle()) {
                RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
                raid.updateBattleContext(battle, b -> b.broadcastChatMessage(Component.translatable("battle.cobblemonraiddens.raid_energy", origin.getName())));
            }

            origin.getEntity().cry();

            return Unit.INSTANCE;
        });
    }
}
