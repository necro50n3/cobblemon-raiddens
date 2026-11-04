package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * An event that is fired when a raid or raid adventure ends.
 */
public record RaidEndEvent(List<ServerPlayer> players, RaidBoss raidBoss, boolean isWin) {}
