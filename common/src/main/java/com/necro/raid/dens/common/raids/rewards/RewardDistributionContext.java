package com.necro.raid.dens.common.raids.rewards;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.registry.CustomRaidRegistries;
import kotlin.Pair;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class RewardDistributionContext {
    private final List<ServerPlayer> success;
    private final List<ServerPlayer> fail;

    public  RewardDistributionContext(List<ServerPlayer> players, RaidInstance raid) {
        RewardDistributor distributor = CustomRaidRegistries.REWARD_DIST_REGISTRY.get(CobblemonRaidDens.CONFIG.reward_distribution);
        Pair<List<ServerPlayer>, List<ServerPlayer>> result = distributor.distribute(players, raid);
        this.success = result.getFirst();
        this.fail = result.getSecond();
    }

    public List<ServerPlayer> success() {
        return this.success;
    }

    public List<ServerPlayer> fail() {
        return this.fail;
    }
}
