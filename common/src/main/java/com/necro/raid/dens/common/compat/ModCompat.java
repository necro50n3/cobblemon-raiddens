package com.necro.raid.dens.common.compat;

import kotlin.jvm.functions.Function2;

public enum ModCompat {
    COBBLEMON("cobblemon"),
    COBBLEDOLLARS("cobbledollars"),
    MEGA_SHOWDOWN("mega_showdown"),
    RCT_API("rctapi"),
    SIZE_VARIATIONS("cobblemonsizevariation"),
    IRIS("iris"),
    VOCALIZED("cobblemon_vocalized"),
    SHADOWED_HEARTS("shadowedhearts"),
    CREATE("create"),
    MEKANISM("mekanism");

    private static Function2<String, String, Integer> VERSION_COMPARATOR = null;

    private final String modid;
    private boolean loaded;
    private String version;

    ModCompat(String modid) {
        this.modid = modid;
        this.loaded = false;
        this.version = null;
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

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = this.strip(version);
    }

    public boolean isNewerThanOrEqual(String version) {
        if (!this.isLoaded() || this.version == null || VERSION_COMPARATOR == null) return false;
        return this.compare(version) >= 0;
    }

    public boolean isOlderThanOrEqual(String version) {
        if (!this.isLoaded() || this.version == null || VERSION_COMPARATOR == null) return false;
        return this.compare(version) <= 0;
    }

    private int compare(String version) {
        return VERSION_COMPARATOR.invoke(this.version, this.strip(version));
    }

    private String strip(String version) {
        return version.split("[-+]")[0];
    }

    public static void setComparator(Function2<String, String, Integer> versionComparator) {
        VERSION_COMPARATOR = versionComparator;
    }
}
