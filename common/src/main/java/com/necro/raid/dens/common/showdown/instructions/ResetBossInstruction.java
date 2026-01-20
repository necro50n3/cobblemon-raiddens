package com.necro.raid.dens.common.showdown.instructions;

import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.InterpreterInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ResetBossInstruction implements InterpreterInstruction {
    private final BattleMessage message;
    private final BattlePokemon pokemon;
    private final BattlePokemon origin;

    public ResetBossInstruction(PokemonBattle battle, BattleMessage message) {
        this.message = message;
        this.pokemon = this.message.battlePokemon(0, battle);
        this.origin = this.message.pokemonByUuid(1, battle);
    }

    @Override
    public void invoke(@NotNull PokemonBattle battle) {
        if (this.pokemon == null || this.origin == null) return;
        else if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();

        battle.dispatchGo(() -> {
            battle.broadcastChatMessage(Component.translatable("battle.cobblemonraiddens.reset.boss", origin.getName()));

            BattleContext.Type boostBucket = BattleContext.Type.BOOST;
            BattleContext context = ShowdownInterpreter.INSTANCE.getContextFromAction(this.message, boostBucket, battle);

            raid.updateBattleState(battle, battleState -> battleState.bossSide.pokemon.clearNegativeBoosts());
            raid.updateBattleContext(battle, b -> {
                b.broadcastChatMessage(Component.translatable("battle.cobblemonraiddens.reset.boss", origin.getName()));
                BattlePokemon pokemon = b.getSide2().getActivePokemon().getFirst().getBattlePokemon();
                if (pokemon == null) return;
                pokemon.getContextManager().add(context);
            });

            this.pokemon.getContextManager().add(context);
            battle.getMinorBattleActions().put(this.pokemon.getUuid(), this.message);
            return Unit.INSTANCE;
        });
    }
}
