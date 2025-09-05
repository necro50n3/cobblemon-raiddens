package com.necro.raid.dens.common.raids;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public enum RaidType implements StringRepresentable {
    NONE("none", DyeColor.GRAY.getTextureDiffuseColor()),
    NORMAL("normal", DyeColor.WHITE.getTextureDiffuseColor()),
    FIGHTING("fighting", DyeColor.BROWN.getTextureDiffuseColor()),
    FLYING("flying", DyeColor.LIGHT_GRAY.getTextureDiffuseColor()),
    POISON("poison", DyeColor.PURPLE.getTextureDiffuseColor()),
    GROUND("ground", DyeColor.BROWN.getTextureDiffuseColor()),
    ROCK("rock", DyeColor.BROWN.getTextureDiffuseColor()),
    BUG("bug", DyeColor.LIME.getTextureDiffuseColor()),
    GHOST("ghost", DyeColor.MAGENTA.getTextureDiffuseColor()),
    STEEL("steel", DyeColor.LIGHT_GRAY.getTextureDiffuseColor()),
    FIRE("fire", DyeColor.RED.getTextureDiffuseColor()),
    WATER("water", DyeColor.BLUE.getTextureDiffuseColor()),
    GRASS("grass", DyeColor.GREEN.getTextureDiffuseColor()),
    ELECTRIC("electric", DyeColor.YELLOW.getTextureDiffuseColor()),
    PSYCHIC("psychic", DyeColor.PINK.getTextureDiffuseColor()),
    ICE("ice", DyeColor.LIGHT_BLUE.getTextureDiffuseColor()),
    DRAGON("dragon", DyeColor.BLUE.getTextureDiffuseColor()),
    DARK("dark", DyeColor.BLACK.getTextureDiffuseColor()),
    FAIRY("fairy", DyeColor.PINK.getTextureDiffuseColor()),
    STELLAR("stellar", DyeColor.WHITE.getTextureDiffuseColor());

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

    public boolean isPresent() {
        return this.isPresent;
    }

    public void setPresent() {
        this.isPresent = true;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.id;
    }

    public static RaidType fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return null; }
    }

    public static Codec<RaidType> codec() {
        return Codec.STRING.xmap(RaidType::fromString, Enum::name);
    }
}
