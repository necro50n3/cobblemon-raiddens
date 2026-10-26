package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.server.level.ServerPlayer;

public record RaidRewardPostEvent(RaidBoss raidBoss, ServerPlayer player) {}
