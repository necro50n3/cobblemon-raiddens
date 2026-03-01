package com.necro.raid.dens.common.data.raid;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;

public enum RaidType implements StringRepresentable {
    NONE("none", 4079166),
    NORMAL("normal", 16383998),
    FIGHTING("fighting", 16742955),
    FLYING("flying", 11390705),
    POISON("poison", 6109847),
    GROUND("ground", 10446848),
    ROCK("rock", 11889741),
    BUG("bug", 8107086),
    GHOST("ghost", 4398799),
    STEEL("steel", 8559783),
    FIRE("fire", 13842747),
    WATER("water", 4805581),
    GRASS("grass", 3007286),
    ELECTRIC("electric", 15251206),
    PSYCHIC("psychic", 11608720),
    ICE("ice", 9362175),
    DRAGON("dragon", 1069463),
    DARK("dark", 657930),
    FAIRY("fairy", 14111371),
    STELLAR("stellar", 16383998);

    private final String id;
    private final int color;
    private boolean isPresent;

    RaidType(String id, int color) {
        this.id = id;
        this.color = color;
        this.isPresent = false;
    }

    public int getColor() {
        return this.color;
    }

    public Vector4f getVectorColor() {
        return this.getVectorColor(0.5F);
    }

    public Vector4f getVectorColor(float alpha) {
        return new Vector4f(
            ((this.getColor() >> 16) & 0xFF) / 255F,
            ((this.getColor() >> 8) & 0xFF) / 255F,
            (this.getColor() & 0xFF) / 255F,
            alpha
        );
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void setPresent() {
        this.isPresent = true;
    }

    public void setPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public static RaidType fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return NONE; }
    }

    public static Codec<RaidType> codec() {
        return Codec.STRING.xmap(RaidType::fromString, Enum::name);
    }
}
