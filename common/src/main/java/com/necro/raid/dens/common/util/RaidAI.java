package com.necro.raid.dens.common.util;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.ai.RandomBattleAI;
import com.cobblemon.mod.common.battles.ai.StrongBattleAI;
import com.mojang.serialization.Codec;
import com.necro.raid.dens.common.compat.ModCompat;
import com.necro.raid.dens.common.compat.rctapi.RaidDensRCTCompat;
import com.necro.raid.dens.common.raids.RaidTier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum RaidAI implements StringRepresentable {
    RANDOM(RandomBattleAI::new),
    STRONG_5(() -> new StrongBattleAI(5)),
    RCT(() -> ModCompat.RCT_API.isLoaded() ? RaidDensRCTCompat.getRctApi() : new StrongBattleAI(5));

    private final Supplier<BattleAI> supplier;

    RaidAI(Supplier<BattleAI> supplier) {
        this.supplier = supplier;
    }

    public BattleAI create() {
        return this.supplier.get();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name();
    }

    public static RaidAI fromString(String name) {
        try { return valueOf(name); }
        catch (IllegalArgumentException e) { return RANDOM; }
    }

    public static Codec<RaidTier> codec() {
        return Codec.STRING.xmap(RaidTier::fromString, Enum::name);
    }
}
