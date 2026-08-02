package com.necro.raid.dens.common.data.raid;

import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import net.minecraft.nbt.CompoundTag;

public record RaidResetContext(long gameTime, long systemTime, long globalCycle) {
    public RaidResetContext(long gameTime) {
        this(gameTime, RaidHelper.getGlobalCycle().globalCycle);
    }

    public RaidResetContext(long gameTime, long globalCycle) {
        this(gameTime, System.currentTimeMillis(), globalCycle);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLong("game_time", this.gameTime);
        compoundTag.putLong("system_time", this.systemTime);
        compoundTag.putLong("global_cycle", this.globalCycle);
        return compoundTag;
    }

    public long nextReset(long gameTime) {
        return switch (CobblemonRaidDens.CONFIG.reset_mode) {
            case GAME_TIME -> Math.max(0L, CobblemonRaidDens.CONFIG.reset_time * 20L - (gameTime - this.gameTime));
            case SYSTEM_TIME -> Math.max(0L, CobblemonRaidDens.CONFIG.reset_time * 1000L - (System.currentTimeMillis() - this.systemTime)) / 50L;
            case GLOBAL_GAME_TIME -> Math.max(0L, CobblemonRaidDens.CONFIG.reset_time * 20L - (gameTime - RaidHelper.getGlobalCycle().gameTime));
            case GLOBAL_SYSTEM_TIME -> Math.max(0L, CobblemonRaidDens.CONFIG.reset_time * 1000L - (System.currentTimeMillis() - RaidHelper.getGlobalCycle().systemTime)) / 50L;
        };
    }

    public boolean shouldReset(long gameTime) {
        return switch (CobblemonRaidDens.CONFIG.reset_mode) {
            case GAME_TIME -> gameTime - this.gameTime > CobblemonRaidDens.CONFIG.reset_time * 20L;
            case SYSTEM_TIME -> System.currentTimeMillis() - this.systemTime > CobblemonRaidDens.CONFIG.reset_time * 1000L;
            default -> RaidHelper.getGlobalCycle().globalCycle > this.globalCycle;
        };
    }

    public static RaidResetContext load(CompoundTag compoundTag) {
        long gameTime = compoundTag.getLong("game_time");
        long systemTime = compoundTag.getLong("system_time");
        long globalCycle = compoundTag.getLong("global_cycle");
        return new RaidResetContext(gameTime, systemTime, globalCycle);
    }
}
