package com.necro.raid.dens.common.raids;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum RaidCycleMode implements StringRepresentable {
    NONE("none"),
    LOCK_TIER("lock_tier"),
    ALL("all");

    private final String id;

    RaidCycleMode(String id) {
        this.id = id;
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
