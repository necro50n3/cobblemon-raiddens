package com.necro.raid.dens.common.raids;

import com.mojang.serialization.Codec;

public enum RaidFeature {
    DEFAULT("default"),
    MEGA("mega"),
    TERA("tera"),
    DYNAMAX("dynamax");

    private final String id;

    RaidFeature(String id) {
        this.id = id;
    }

    public String getLootTableId() {
        return "raids/features/" + this.id;
    }

    public String getTranslatable() {
        return "feature.cobblemonraiddens." + this.id;
    }

    public static RaidFeature fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static Codec<RaidFeature> codec() {
        return Codec.STRING.xmap(RaidFeature::fromString, Enum::name);
    }
}
