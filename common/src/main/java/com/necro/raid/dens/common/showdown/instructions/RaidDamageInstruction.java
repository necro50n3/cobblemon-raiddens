package com.necro.raid.dens.common.showdown.instructions;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.interpreter.Effect;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.api.pokemon.status.Status;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.battles.ShowdownInterpreter;
import com.cobblemon.mod.common.battles.dispatch.*;
import com.cobblemon.mod.common.battles.interpreter.instructions.MoveInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.status.PersistentStatusContainer;
import com.cobblemon.mod.common.pokemon.status.statuses.persistent.PoisonStatus;
import com.cobblemon.mod.common.util.LocalizationUtilsKt;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.cobblemon.mod.common.util.MiscUtilsKt.cobblemonResource;

public class RaidDamageInstruction implements ActionEffectInstruction {
    private final InstructionSet instructionSet;
    private final BattleActor actor;
    private final BattleMessage publicMessage;
    private final BattleMessage privateMessage;
    private final BattlePokemon expectedTarget;
    private CompletableFuture<?> future;
    private Set<String> holds;

    public RaidDamageInstruction(InstructionSet instructionSet, BattleActor actor, BattleMessage publicMessage, BattleMessage privateMessage) {
        this.instructionSet = instructionSet;
        this.actor = actor;
        this.publicMessage = publicMessage;
        this.privateMessage = privateMessage;
        this.expectedTarget = this.publicMessage.battlePokemon(0, this.actor.getBattle());
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
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "raid_damage");
    }

    @Override
    public void preActionEffect(@NotNull PokemonBattle battle) {
        if (this.expectedTarget == null) return;

        BattlePokemon source = privateMessage.battlePokemonFromOptional(battle, "of");
        if (source != null) {
            Effect effect = privateMessage.effect("from");
            ShowdownInterpreter.INSTANCE.broadcastOptionalAbility(battle, effect, source);
        }
    }

    @Override
    public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
        battle.dispatch(() -> {
            Effect effect = this.privateMessage.effect("from");
            Status status = effect == null ? null : Statuses.getStatus(effect.getId());

            BattlePokemon pokemon = this.privateMessage.battlePokemon(0, battle);
            if (pokemon == null) return DispatchResultKt.getGO();
            if (status instanceof PoisonStatus) {
                PersistentStatusContainer container = pokemon.getEffectedPokemon().getStatus();
                status = container == null ? status : container.getStatus();
            }

            ActionEffectTimeline actionEffect = null;
            if (status != null) actionEffect = status.getActionEffect();
            if (actionEffect == null && effect != null) {
                String key = "damage_" + effect.getId();
                actionEffect = ActionEffects.INSTANCE.getActionEffects().get(cobblemonResource(key));
            }
            if (actionEffect == null) {
                actionEffect = ActionEffects.INSTANCE.getActionEffects().get(cobblemonResource("generic_damage"));
            }
            if (actionEffect == null) return DispatchResultKt.getGO();

            List<Object> providers = new ArrayList<>(List.of(battle));
            providers.add(new UsersProvider(this.expectedTarget.getEffectedPokemon().getEntity()));

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
        RaidInstance raidInstance = ((IRaidBattle) battle).getRaidBattle();
        BattlePokemon battlePokemon = this.publicMessage.battlePokemon(0, this.actor.battle);
        if (battlePokemon == null || battlePokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) battlePokemon.getEntity()).isRaidBoss()) return;

        String args = this.privateMessage.argumentAt(1);
        assert args != null;
        String damageStr = args.split(" ")[0];
        CauserInstruction lastCauser = this.instructionSet.getMostRecentCauser(this);
        Effect effect = this.privateMessage.effect("from");

        battle.dispatch(() -> {
            Component pokemonName = battlePokemon.getName();
            BattlePokemon source = this.privateMessage.battlePokemonFromOptional(battle, "of");

            if (effect != null) {
                Component lang = null;

                if (Set.of("brn", "psn", "tox").contains(effect.getId())) {
                    Status status = Statuses.getStatus(effect.getId());
                    if (status != null) lang = LocalizationUtilsKt.lang(String.format("status.%s.hurt", status.getName().getPath()), pokemonName);
                }
                else if (Set.of("aftermath", "innardsout").contains(effect.getId())) lang = LocalizationUtilsKt.battleLang("damage.generic", pokemonName);
                else if (Set.of("chloroblast", "steelbeam").contains(effect.getId())) lang = LocalizationUtilsKt.battleLang("damage.mindblown", pokemonName);
                else if (effect.getId().equals("jumpkick")) lang = LocalizationUtilsKt.battleLang("damage.highjumpkick", pokemonName);
                else lang = LocalizationUtilsKt.battleLang("damage." + effect.getId(), pokemonName, source != null ? source.getName() : Component.literal("UNKOWN"));

                if (lang != null) battle.broadcastChatMessage(lang);
            }

            float damage = Float.parseFloat(damageStr);
            boolean causedFaint = raidInstance.getRemainingHealth() < damage;
            if (causedFaint) battlePokemon.getEffectedPokemon().setCurrentHealth(0);

            ServerPlayer player = battle.getPlayers().getFirst();
            raidInstance.syncHealth(player, battle, damage);
            battlePokemon.getEffectedPokemon().setCurrentHealth((int) raidInstance.getRemainingHealth());


            if (lastCauser instanceof MoveInstruction && ((MoveInstruction) lastCauser).getActionEffect() != null && !causedFaint) {
                return new UntilDispatch(() -> ((MoveInstruction) lastCauser).getFuture().isDone());
            }
            else if (causedFaint) return DispatchResultKt.getGO();
            else return new UntilDispatch(() -> !getHolds().contains("effects"));
        });
    }
}
