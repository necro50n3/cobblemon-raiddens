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

public class ResetPlayerInstruction implements ActionEffectInstruction {
    private final BattleMessage message;
    private CompletableFuture<?> future;
    private Set<String> holds;
    private final BattlePokemon pokemon;
    private final BattlePokemon origin;

    public ResetPlayerInstruction(PokemonBattle battle, BattleMessage message) {
        this.message = message;
        this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
        this.holds = new HashSet<>();
        this.pokemon = this.message.battlePokemon(0, battle);
        this.origin = this.message.battlePokemon(1, battle);
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
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "reset_player");
    }

    @Override
    public void preActionEffect(@NotNull PokemonBattle battle) {}

    @Override
    public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
        if (this.pokemon == null || this.origin == null) return;
        battle.dispatch(() -> {
            ActionEffectTimeline actionEffect = ActionEffects.INSTANCE.getActionEffects().get(cobblemonResource("unboost"));
            List<Object> providers = new ArrayList<>(List.of(battle));
            providers.add(new UsersProvider(this.pokemon.getEffectedPokemon().getEntity()));

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
        if (this.pokemon == null || this.origin == null) return;
        battle.dispatch(() -> {
            battle.broadcastChatMessage(Component.translatable("battle.cobblemonraiddens.reset.boss", origin.getName()));

            BattleContext.Type boostBucket = BattleContext.Type.UNBOOST;
            BattleContext context = ShowdownInterpreter.INSTANCE.getContextFromAction(this.message, boostBucket, battle);

            this.pokemon.getContextManager().add(context);
            battle.getMinorBattleActions().put(this.pokemon.getUuid(), this.message);
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
