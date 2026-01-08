package com.necro.raid.dens.common.raids;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum RaidState implements StringRepresentable {
    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    FAILED("failed"),
    SUCCESS("success"),
    CANCELLED("cancelled");

    private final String id;

    RaidState(String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public static RaidState fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return NOT_STARTED; }
    }
}
