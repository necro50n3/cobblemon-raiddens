package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * An event that is fired when a raid battle ends for a specific player.
 * <p>
 * Refer to the relevant events to modify or cancel specific events.
 * @see ModifyCatchRateEvent
 * @see ModifyShinyRateEvent
 * @see OpenPouchEvent
 * @see RaidRewardPostEvent
 * @see RewardPokemonEvent
 */
public record RaidBattleEndEvent(ServerPlayer player, RaidBoss raidBoss, @Nullable Pokemon pokemon, boolean itemReward, float catchRate, boolean isWin) {}
