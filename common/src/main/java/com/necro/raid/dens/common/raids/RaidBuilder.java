package com.necro.raid.dens.common.raids;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.necro.raid.dens.common.config.TierConfig;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.util.RaidUtils;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RaidBuilder {
    public static BattleStartResult build(ServerPlayer player, PokemonEntity pokemonEntity, @Nullable UUID leadingPokemon, RaidBoss boss, TierConfig config) {
        List<BattlePokemon> battleTeam = PlayerExtensionsKt.party(player)
            .toBattleTeam(false, false, leadingPokemon)
            .stream().filter(p ->
                !p.getEffectedPokemon().isFainted()
                && !RaidUtils.isPokemonBlacklisted(p.getEffectedPokemon())
                && !RaidUtils.isAbilityBlacklisted(p.getEffectedPokemon().getAbility())
            ).toList();
        if (!battleTeam.isEmpty()) battleTeam = battleTeam.subList(0, Mth.clamp(battleTeam.size(), 1, config.raidPartySize()));
        PlayerBattleActor playerActor = new PlayerBattleActor(player.getUUID(), battleTeam);
        PokemonBattleActor wildActor = new PokemonBattleActor(pokemonEntity.getPokemon().getUuid(),
            new BattlePokemon(pokemonEntity.getPokemon(), pokemonEntity.getPokemon(), p -> Unit.INSTANCE),
            Cobblemon.config.getDefaultFleeDistance(), boss.getRaidAI().create()
        );
        BattleFormat battleFormat = BattleFormat.Companion.getGEN_9_SINGLES();
        ErroredBattleStart errors = new ErroredBattleStart();

        if (!battleTeam.isEmpty() && battleTeam.getFirst().getHealth() <= 0) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.insufficientPokemon(
                player,
                battleFormat.getBattleType().getSlotsPerActor(),
                playerActor.getPokemonList().size()
            ));
        }

        if (playerActor.getPokemonList().stream().anyMatch(battlePokemon ->
            battlePokemon.getEntity() != null && battlePokemon.getEntity().isBusy()
        )) {
            player.getDisplayName();
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.targetIsBusy(player.getDisplayName()));
        }

        if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
            errors.getParticipantErrors().get(playerActor).add(BattleStartError.Companion.alreadyInBattle(playerActor));
        }

        playerActor.setBattleTheme(pokemonEntity.getBattleTheme());

        if (errors.isEmpty()) {
            return BattleRegistry.startBattle(battleFormat, new BattleSide(playerActor), new BattleSide(wildActor), true);
        }
        else return errors;
    }
}
