package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record RaidEndEvent(List<ServerPlayer> players, RaidBoss raid, boolean isWin) {}
