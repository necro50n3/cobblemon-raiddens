package com.necro.raid.dens.common.showdown.instructions;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BasicContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleContext;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.interpreter.instructions.BoostInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
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

public class RaidBoostInstruction implements ActionEffectInstruction {
    private final BattleMessage message;
    private CompletableFuture<?> future;
    private Set<String> holds;
    private final boolean isBoost;
    private final BattlePokemon pokemon;
    private final String statKey;
    private final int stages;
    private final Component stat;

    @SuppressWarnings("ConstantConditions")
    public RaidBoostInstruction(PokemonBattle battle, BattleMessage message, boolean isBoost) {
        this.message = message;
        this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
        this.holds = new HashSet<>();
        this.isBoost = isBoost;
        this.pokemon = this.message.battlePokemon(0, battle);
        this.statKey = this.message.argumentAt(1);
        this.stages = Integer.parseInt(this.message.argumentAt(2));
        this.stat = Stats.Companion.getStat(this.statKey).getDisplayName();
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
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_boost");
    }

    @Override
    public void preActionEffect(@NotNull PokemonBattle battle) {}

    @Override
    public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
        if (this.pokemon == null || this.stages == 0) return;
        battle.dispatch(() -> {
            ActionEffectTimeline actionEffect = this.isBoost ? BoostInstruction.Companion.getBOOST_EFFECT() : BoostInstruction.Companion.getUNBOOST_EFFECT();
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
        String severity = Stats.Companion.getSeverity(this.stages);
        String rootKey = this.isBoost ? "boost" : "unboost";

        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        RaidInstance raid = ((IRaidBattle) battle).crd_getRaidBattle();

        battle.dispatch(() -> {
            Component lang;
            if (this.message.hasOptionalArgument("zeffect")) {
                lang = LocalizationUtilsKt.battleLang(String.format("%s.%s.zeffect", rootKey, severity), this.pokemon.getName(), this.stat);
            }
            else {
                lang = LocalizationUtilsKt.battleLang(String.format("%s.%s", rootKey, severity), this.pokemon.getName(), this.stat);
            }
            battle.broadcastChatMessage(lang);

            BattleContext.Type boostBucket = this.isBoost ? BattleContext.Type.BOOST : BattleContext.Type.UNBOOST;
            BattleContext context = ShowdownInterpreter.INSTANCE.getContextFromAction(this.message, boostBucket, battle);
            this.pokemon.getContextManager().add(context);

            raid.updateBattleState(battle, battleState -> battleState.bossSide.pokemon.boost(Stats.Companion.getStat(this.statKey), this.isBoost ? stages : -stages));
            raid.updateBattleContext(battle, b -> {
                BattlePokemon pokemon = b.getSide2().getActivePokemon().getFirst().getBattlePokemon();
                if (pokemon == null) return;
                b.broadcastChatMessage(lang);
                BattleContext ctx = new BasicContext(this.statKey, b.getTurn(), boostBucket, null);
                for (int i = 0; i < this.stages; i++) pokemon.getContextManager().add(ctx);
            });

            battle.getMinorBattleActions().put(this.pokemon.getUuid(), this.message);
            return new UntilDispatch(() -> !this.holds.contains("effects"));
        });
    }
}
