package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.RaidSession;
import net.minecraft.server.level.ServerPlayer;

public record RaidRewardPostEvent(RaidSession session, ServerPlayer player) {}
