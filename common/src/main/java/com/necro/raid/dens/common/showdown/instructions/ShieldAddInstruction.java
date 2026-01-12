package com.necro.raid.dens.common.showdown.instructions;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidBattle;
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

public class ShieldAddInstruction implements ActionEffectInstruction {
    private final BattleMessage message;
    private CompletableFuture<?> future;
    private Set<String> holds;
    private final BattlePokemon pokemon;

    public ShieldAddInstruction(PokemonBattle battle, BattleMessage message) {
        this.message = message;
        this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
        this.holds = new HashSet<>();
        this.pokemon = this.message.battlePokemon(0, battle);
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
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "shield_add");
    }

    @Override
    public void preActionEffect(@NotNull PokemonBattle battle) {}

    @Override
    public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
        if (this.pokemon == null) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        if (raid == null) return;

        battle.dispatch(() -> {
            ActionEffectTimeline actionEffect = ActionEffects.INSTANCE.getEffectWithBattleContext(cobblemonResource("protect"), this.pokemon);
            if (actionEffect == null) actionEffect = ActionEffects.INSTANCE.getActionEffects().get(cobblemonResource("generic_move"));
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
        if (this.pokemon == null || this.pokemon.getEntity() == null) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();
        if (raid == null) return;

        battle.dispatch(() -> {
            Component lang = Component.translatable("cobblemonraiddens.status.shield.apply", this.pokemon.getName());
            battle.broadcastChatMessage(lang);
            raid.updateBattleContext(battle, b -> b.broadcastChatMessage(lang));

            battle.getMinorBattleActions().put(this.pokemon.getUuid(), this.message);
            return new UntilDispatch(() -> !this.holds.contains("effects"));
        });
    }
}
