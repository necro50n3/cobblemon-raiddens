package com.necro.raid.dens.common.data.raid;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum RaidFeature implements StringRepresentable {
    DEFAULT("default"),
    MEGA("mega"),
    TERA("tera"),
    DYNAMAX("dynamax");

    private final String id;

    RaidFeature(String id) {
        this.id = id;
    }

    public String getLootTableId() {
        return "raid/feature/" + this.id;
    }

    public String getTranslatable() {
        return "feature.cobblemonraiddens." + this.id;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public static RaidFeature fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return DEFAULT; }
    }

    public static Codec<RaidFeature> codec() {
        return Codec.STRING.xmap(RaidFeature::fromString, Enum::name);
    }
}
