package com.necro.raid.dens.common.raids.rewards;

import com.necro.raid.dens.common.raids.RaidInstance;
import kotlin.Pair;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

@FunctionalInterface
public interface RewardDistributor {
    Pair<List<ServerPlayer>, List<ServerPlayer>> distribute(List<ServerPlayer> players, RaidInstance raid);
}
