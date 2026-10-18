package com.necro.raid.dens.common.events;

import com.necro.raid.dens.common.data.RaidSession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public class RaidDenSpawnEvent {
    private final ServerLevel level;
    private final BlockPos blockPos;
    private final RaidSession raid;

    public RaidDenSpawnEvent(@NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull RaidSession raid) {
        this.level = level;
        this.blockPos = blockPos;
        this.raid = raid;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public RaidSession getRaid() {
        return this.raid;
    }
}
