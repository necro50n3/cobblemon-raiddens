package com.necro.raid.dens.common.data.raid;

import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum RaidCycleMode implements StringRepresentable {
    NONE("none", true, true),
    BUCKET("bucket", false, false),
    LOCK_BOTH("lock_both", true, true),
    LOCK_TIER("lock_tier", true, false),
    LOCK_TYPE("lock_type", false, true),
    ALL("all", false, false),
    CONFIG("config", true, true);

    private final String id;
    private final boolean lockTier;
    private final boolean lockType;

    RaidCycleMode(String id, boolean lockTier, boolean lockType) {
        this.id = id;
        this.lockTier = lockTier;
        this.lockType = lockType;
    }

    public boolean canCycleTier() {
        if (this == CONFIG) {
            String cycle = CobblemonRaidDens.CONFIG.cycle_mode;
            if (cycle.equals("CONFIG")) return !this.lockTier;
            else return !fromString(cycle).lockTier;
        }
        return !this.lockTier;
    }

    public boolean canCycleType() {
        if (this == CONFIG) {
            String cycle = CobblemonRaidDens.CONFIG.cycle_mode;
            if (cycle.equals("CONFIG")) return !this.lockType;
            else return !fromString(cycle).lockType;
        }
        return !this.lockType;
    }

    public static RaidCycleMode fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return NONE; }
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }
}
