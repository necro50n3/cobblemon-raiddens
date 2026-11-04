package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.server.level.ServerPlayer;

/**
 * An event that is fired after a player has received their rewards for a raid battle.
 */
public record RaidRewardPostEvent(RaidBoss raidBoss, boolean itemReward, ServerPlayer player) {}
