package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.asymmetric.battles.common.api.BattleParticipant;
import com.necro.raid.dens.common.data.raid.RaidBattleFormat;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import com.necro.raid.dens.common.util.ITransformer;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidBuilder {
    public static BattleStartResult build(ServerPlayer player, PokemonEntity pokemonEntity, @Nullable UUID leadingPokemon, RaidBoss boss) {
        List<BattlePokemon> battleTeam = PlayerExtensionsKt.party(player)
            .toBattleTeam(false, false, leadingPokemon)
            .stream().filter(p ->
                !p.getEffectedPokemon().isFainted()
                && !RaidUtils.isPokemonBlacklisted(p.getEffectedPokemon())
                && !RaidUtils.isAbilityBlacklisted(p.getEffectedPokemon().getAbility())
                && !RaidUtils.isHeldItemBlacklisted(p.getEffectedPokemon().heldItem())
            ).toList();
        if (!battleTeam.isEmpty()) battleTeam = battleTeam.subList(0, Mth.clamp(battleTeam.size(), 1, boss.getRaidPartySize()));
        PlayerBattleActor playerActor = new PlayerBattleActor(player.getUUID(), battleTeam);
        PokemonBattleActor wildActor = new PokemonBattleActor(pokemonEntity.getPokemon().getUuid(),
            new BattlePokemon(pokemonEntity.getPokemon(), pokemonEntity.getPokemon(), p -> Unit.INSTANCE),
            Cobblemon.config.getDefaultFleeDistance() * 5, CustomRaidRegistries.AI_REGISTRY.get(boss.getRaidAI()).get()
        );
        setTransformTarget(pokemonEntity, wildActor);

        BattleFormat battleFormat = boss.getBattleFormat().format();
        ErroredBattleStart errors = new ErroredBattleStart();

        if (!battleTeam.isEmpty() && battleTeam.getFirst().getHealth() <= 0) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.insufficientPokemon(
                player,
                battleFormat.getBattleType().getSlotsPerActor(),
                playerActor.getPokemonList().size()
            ));
        }

        if (playerActor.getPokemonList().stream().anyMatch(battlePokemon -> battlePokemon.getEntity() != null && battlePokemon.getEntity().isBusy())) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.targetIsBusy(player.getDisplayName()));
        }

        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.alreadyInBattle(playerActor));
        }

        playerActor.setBattleTheme(boss.getMusic());

        if (errors.isEmpty()) {
            BattleSide side1;
            BattleSide side2;
            if (boss.getBattleFormat() == RaidBattleFormat.RAID) {
                side1 = new BattleSide(playerActor, BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor());
                side2 = new BattleSide(wildActor, BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor(), BattleParticipant.dummy().toActor());
            }
            else {
                side1 = new BattleSide(playerActor);
                side2 = new BattleSide(wildActor);
            }

            return BattleRegistry.startBattle(battleFormat, side1, side2, true);
        }
        else return errors;
    }

    private static void setTransformTarget(PokemonEntity pokemonEntity, PokemonBattleActor wildActor) {
        Pokemon transformTarget = ((ITransformer) pokemonEntity).crd_getTransformTarget();
        if (transformTarget != null) ((ITransformer) wildActor).crd_setTransformTarget(transformTarget);
    }
}
