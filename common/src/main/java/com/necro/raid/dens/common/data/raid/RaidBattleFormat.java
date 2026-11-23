package com.necro.raid.dens.common.data.raid;

import com.cobblemon.mod.common.battles.BattleFormat;
import com.mojang.serialization.Codec;
import com.necro.asymmetric.battles.common.api.AsymmetricBattleFormats;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum RaidBattleFormat implements StringRepresentable {
    SINGLES(AsymmetricBattleFormats.GEN_9_RAID_SINGLES),
    DOUBLES(AsymmetricBattleFormats.GEN_9_RAID_DOUBLES),
    TRIPLES(AsymmetricBattleFormats.GEN_9_RAID_TRIPLES),
    QUADRUPLES(AsymmetricBattleFormats.GEN_9_RAID_QUADRUPLES),
    PENTUPLES(AsymmetricBattleFormats.GEN_9_RAID_PENTUPLES),
    SEXTUPLES(AsymmetricBattleFormats.GEN_9_RAID_SEXTUPLES),
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
