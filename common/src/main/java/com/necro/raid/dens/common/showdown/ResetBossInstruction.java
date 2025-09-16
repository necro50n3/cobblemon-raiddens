package com.necro.raid.dens.common.showdown;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class ResetBossInstruction implements ActionEffectInstruction {
    private final BattleMessage message;
    private CompletableFuture<?> future;
    private Set<String> holds;

    public ResetBossInstruction(BattleMessage message) {
        this.message = message;
        this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
        this.holds = new HashSet<>();
    }

    @Override
    public @NotNull CompletableFuture<?> getFuture() {
        return this.future;
    }

    @Override
    public void setFuture(@NotNull CompletableFuture<?> future) {
        this.future = future;
    }

    @Override
    public @NotNull Set<String> getHolds() {
        return this.holds;
    }

    @Override
    public void setHolds(@NotNull Set<String> set) {
        this.holds = set;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "reset_boss");
    }

    @Override
    public void preActionEffect(@NotNull PokemonBattle battle) {}

    @Override
    public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
        battle.dispatch(() -> {
            ActionEffectTimeline actionEffect = ActionEffects.INSTANCE.getActionEffects().get(cobblemonResource("boost"));
            List<Object> providers = new ArrayList<>(List.of(battle));
            BattlePokemon battlePokemon = this.message.battlePokemon(0, battle);
            if (battlePokemon != null && battlePokemon.getEffectedPokemon().getEntity() != null)
                providers.add(new UsersProvider(battlePokemon.getEffectedPokemon().getEntity()));

            ActionEffectContext context = new ActionEffectContext(
                actionEffect, new HashSet<>(), providers, runtime, false, false,
                new ArrayList<>(), battle.getPlayers().getFirst().level()
            );

            this.setFuture(actionEffect.run(context));
            this.setHolds(context.getHolds());
            this.future.thenAccept(v -> this.holds.clear());

            return DispatchResultKt.getGO();
        });
    }

    @Override
    public void postActionEffect(@NotNull PokemonBattle battle) {
        battle.dispatch(() -> {
            BattlePokemon battlePokemon = this.message.battlePokemon(0, battle);
            String origin = this.message.argumentAt(1);
            if (battlePokemon == null) return DispatchResultKt.getGO();
            else if (origin == null) return DispatchResultKt.getGO();

            battle.broadcastChatMessage(
                Component.translatable("battle.cobblemonraiddens.reset.boss", origin)
            );

            BattleContext.Type boostBucket = BattleContext.Type.BOOST;
            BattleContext context = ShowdownInterpreter.INSTANCE.getContextFromAction(this.message, boostBucket, battle);

            battlePokemon.getContextManager().add(context);
            battle.getMinorBattleActions().put(battlePokemon.getUuid(), this.message);
            return new UntilDispatch(() -> !this.holds.contains("effects"));
        });
    }

    @Override
    public void addMolangQueries(@NotNull MoLangRuntime runtime) {
        ActionEffectInstruction.DefaultImpls.addMolangQueries(this, runtime);
    }

    @Override
    public void invoke(@NotNull PokemonBattle battle) {
        ActionEffectInstruction.DefaultImpls.invoke(this, battle);
    }
}
