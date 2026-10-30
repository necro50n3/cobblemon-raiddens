package com.necro.raid.dens.common.data.raid;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.mojang.serialization.Codec;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum RaidBattleFormat implements StringRepresentable {
    SINGLES(BattleFormat.Companion.getGEN_9_SINGLES()),
    DOUBLES(BattleFormat.Companion.getGEN_9_DOUBLES()),
    TRIPLES(BattleFormat.Companion.getGEN_9_TRIPLES()),
    QUADRUPLES(AsymmetricBattleFormats.GEN_9_QUADRUPLES),
    PENTUPLES(AsymmetricBattleFormats.GEN_9_PENTUPLES),
    SEXTUPLES(AsymmetricBattleFormats.GEN_9_SEXTUPLES),
    RAID(AsymmetricBattleFormats.GEN_9_RAID);

    private final BattleFormat format;

    RaidBattleFormat(BattleFormat format) {
        this.format = format;
    }

    public BattleFormat format() {
        return this.format;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.format.getBattleType().getName();
    }

    public static RaidBattleFormat fromString(String name) {
        try { return valueOf(name.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return SINGLES; }
    }

    public static Codec<RaidBattleFormat> codec() {
        return Codec.STRING.xmap(RaidBattleFormat::fromString, Enum::name);
    }
}
