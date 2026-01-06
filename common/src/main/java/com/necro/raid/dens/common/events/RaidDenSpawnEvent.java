package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class RaidDenSpawnEvent {
    private final ServerLevel level;
    private final BlockPos blockPos;
    private final RaidBoss raidBoss;

    public RaidDenSpawnEvent(@NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull RaidBoss raidBoss) {
        this.level = level;
        this.blockPos = blockPos;
        this.raidBoss = raidBoss;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public RaidBoss getRaidBoss() {
        return this.raidBoss;
    }
}
