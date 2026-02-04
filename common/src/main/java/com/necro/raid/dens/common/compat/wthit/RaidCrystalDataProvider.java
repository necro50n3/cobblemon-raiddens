package com.necro.raid.dens.common.compat.wthit;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import mcp.mobius.waila.api.IDataProvider;
import mcp.mobius.waila.api.IDataWriter;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;

public class RaidCrystalDataProvider implements IDataProvider<RaidCrystalBlockEntity> {
    @Override
    public void appendData(IDataWriter data, IServerAccessor<RaidCrystalBlockEntity> accessor, IPluginConfig config) {
        RaidCrystalBlockEntity raidCrystal = accessor.getTarget();
        if (raidCrystal.getPlayerCount() > 0) data.raw().putInt("player_count", raidCrystal.getPlayerCount());
        if (raidCrystal.getTicksUntilNextReset() > 0) data.raw().putString("next_reset", this.formatTicks(raidCrystal.getTicksUntilNextReset()));
    }

    private String formatTicks(long ticks) {
        int totalSeconds = (int) ticks / 20;

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
