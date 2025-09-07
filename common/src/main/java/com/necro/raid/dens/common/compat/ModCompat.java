package com.necro.raid.dens.common.compat;

public enum ModCompat {
    MEGA_SHOWDOWN("mega_showdown");

    private final String modid;
    private boolean loaded;

    ModCompat(String modid) {
        this.modid = modid;
        this.loaded = false;
    }

    public String getModid() {
        return this.modid;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}
