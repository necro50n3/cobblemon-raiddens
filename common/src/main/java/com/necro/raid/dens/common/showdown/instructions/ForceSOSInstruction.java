package com.necro.raid.dens.common.showdown.instructions;

import com.bedrockk.molang.runtime.MoLangRuntime;
import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.drop.DropTable;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectContext;
import com.cobblemon.mod.common.api.moves.animations.ActionEffectTimeline;
import com.cobblemon.mod.common.api.moves.animations.ActionEffects;
import com.cobblemon.mod.common.api.moves.animations.UsersProvider;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.dispatch.ActionEffectInstruction;
import com.cobblemon.mod.common.battles.dispatch.DispatchResult;
import com.cobblemon.mod.common.battles.dispatch.DispatchResultKt;
import com.cobblemon.mod.common.battles.dispatch.UntilDispatch;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.battles.runner.ShowdownService;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.asymmetric.battles.common.api.AsymmetricAPI;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import com.necro.asymmetric.battles.common.util.IBattleSpawn;
import com.necro.asymmetric.battles.common.util.PokemonLocatorUtils;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.compat.ModCompat;
import kotlin.Pair;
import kotlin.Unit;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ForceSOSInstruction implements ActionEffectInstruction {
    private CompletableFuture<?> future;
    private Set<String> holds;
    private final PokemonProperties properties;

    public ForceSOSInstruction(BattleMessage message) {
        this.future = CompletableFuture.completedFuture(Unit.INSTANCE);
        this.holds = new HashSet<>();
        String properties = message.argumentAt(0);
        assert properties != null;
        this.properties = PokemonProperties.Companion.parse(properties + " uncatchable");
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
        return ResourceLocation.fromNamespaceAndPath(CobblemonRaidDens.MOD_ID, "force_sos");
    }

    @Override
    public void preActionEffect(@NotNull PokemonBattle battle) {}

    @Override
    public void runActionEffect(@NotNull PokemonBattle battle, @NotNull MoLangRuntime runtime) {
        if (!ModCompat.SOS_ENCOUNTERS.isLoaded()) return;
        List<ActiveBattlePokemon> pokemonList = battle.getSide2().getActivePokemon().stream().filter(p -> p != null && p.hasPokemon() && p.isAlive()).toList();
        if (pokemonList.size() != 1) return;
        BattlePokemon pokemon = pokemonList.getFirst().getBattlePokemon();
        if (pokemon == null) return;

        battle.dispatch(() -> {
            if (pokemon.getFaintedAt() != null) return DispatchResultKt.getGO();
            battle.broadcastChatMessage(Component.translatable("sosencounters.battle.sos.call", pokemon.getName()));

            ActionEffectTimeline actionEffect = ActionEffects.INSTANCE.getActionEffects().get(ResourceLocation.fromNamespaceAndPath("sosencounters", "sos"));
            List<Object> providers = new ArrayList<>(List.of(battle));
            providers.add(new UsersProvider(pokemon.getEffectedPokemon().getEntity()));

            ActionEffectContext context = new ActionEffectContext(
                actionEffect, new HashSet<>(), providers, runtime, false, false,
                new ArrayList<>(), battle.getPlayers().getFirst().level()
            );

            if (pokemon.getEffectedPokemon().getEntity() != null) {
                this.setFuture(actionEffect.run(context));
                this.setHolds(context.getHolds());
                this.future.thenAccept(v -> this.holds.clear());
            }

            return new UntilDispatch(() -> !this.holds.contains("cry"));
        });

        battle.dispatch(() -> {
            if (pokemon.getFaintedAt() != null) return DispatchResultKt.getGO();
            battle.broadcastChatMessage(Component.literal("... ... ..."));
            return new UntilDispatch(() -> !this.holds.contains("wait"));
        });

        battle.dispatch(() -> {
            if (pokemon.getFaintedAt() != null) return this.fail(battle, pokemon);

            PokemonEntity entity = pokemon.getEntity();
            if (entity == null) return this.fail(battle, pokemon);
            ServerPlayer player;
            if (battle.getSide1().getActors()[0] instanceof PlayerBattleActor playerActor && playerActor.getEntity() != null && !playerActor.getActivePokemon().isEmpty()) player = playerActor.getEntity();
            else return this.fail(battle, pokemon);
            Pair<ServerLevel, Vec3> playerActivePos = playerActor.getActivePokemon().getFirst().getPosition();
            int otherSide = pokemon.getActor() == battle.getSide2().getActors()[0] ? 4 : 2;

            Pokemon spawn = this.properties.create(player);
            Vec3 spawnPos = this.getSendOutPosition(battle, spawn, otherSide);
            if (spawnPos == null) spawnPos = entity.position();
            ServerLevel level = (ServerLevel) entity.level();
            PokemonEntity newEntity = spawn.sendOut(level, spawnPos, null, p -> Unit.INSTANCE);
            if (newEntity == null) return this.fail(battle, pokemon);
            newEntity.lookAt(EntityAnchorArgument.Anchor.EYES, playerActivePos != null ? playerActivePos.getSecond() : player.position());
            level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, spawnPos.x(), spawnPos.y(), spawnPos.z(), 1, 1.0, 0.0, 0.0, 0.0);
            newEntity.playSound(SoundEvents.WIND_CHARGE_BURST.value());
            ((IBattleSpawn) spawn).aba_setBattleSpawn();
            newEntity.setDrops(new DropTable());

            PokemonBattleActor wildActor = BattleParticipant.wild(newEntity).toActor();
            AsymmetricAPI.setMultiBattleActor(wildActor, battle, otherSide);
            newEntity.setBattleId(battle.getBattleId());
            ShowdownService.Companion.getService().send(battle.getBattleId(), new String[]{">p" + otherSide + " move " + newEntity.getRandom().nextInt(1, spawn.getMoveSet().getMoves().size() + 1)});

            battle.broadcastChatMessage(Component.translatable("sosencounters.battle.sos.success", newEntity.getDisplayName()));
            return new UntilDispatch(() -> !this.holds.contains("effects"));
        });
    }

    @Override
    public void postActionEffect(@NotNull PokemonBattle battle) {}

    private DispatchResult fail(PokemonBattle battle, BattlePokemon pokemon) {
        battle.broadcastChatMessage(Component.translatable("sosencounters.battle.sos.failed", pokemon.getName()));
        return DispatchResultKt.getGO();
    }

    private Vec3 getSendOutPosition(PokemonBattle battle, Pokemon pokemon, int side) {
        Vec3 wildPos = PokemonLocatorUtils.getAveragePosition(battle.getSide2().getActors());
        Vec3 playerPos = PokemonLocatorUtils.getAveragePosition(battle.getSide1().getActors());
        if (wildPos == null || playerPos == null) return null;

        Vec3 wildOffset = playerPos.subtract(wildPos);
        double wildDistance = wildOffset.length();
        if (wildDistance == 0.0) return null;

        float width = pokemon.getForm().getHitbox().width();
        float scale = pokemon.getForm().getBaseScale();
        float pokemonWidth = width * scale;

        Vec3 orthogonalVector = new Vec3(wildOffset.x, 0.0, wildOffset.z).normalize();
        if (orthogonalVector.lengthSqr() == 0.0) return null;
        orthogonalVector = orthogonalVector.cross(new Vec3(0.0, 1.0, 0.0));

        double sideOffset = ((side == 4 ? 1 : 0) - 0.5) * Math.max(pokemonWidth, 3.5);
        return wildPos.add(orthogonalVector.scale(sideOffset)).add(0.0, 1.0, 0.0);
    }
}
