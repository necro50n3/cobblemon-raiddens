package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.pokemon.EVs;
import com.cobblemon.mod.common.pokemon.PokemonStats;
import com.necro.raid.dens.common.util.IEVExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(PokemonStats.class)
public abstract class EVsMixin implements IEVExtension {
    @Final
    @Shadow
    private Map<Stat, Integer> stats;

    @Shadow
    public abstract int total();

    @Shadow
    public abstract void update();

    @Override
    public void crd_forceSet(Stat stat, int value) {
        this.stats.put(stat, value);
        this.update();
    }

    @Override
    public void crd_validate() {
        if (this.stats.values().stream().anyMatch(value -> value > EVs.MAX_STAT_VALUE)) throw new IllegalArgumentException("EV cannot exceed " + EVs.MAX_STAT_VALUE);
        if (this.total() > EVs.MAX_TOTAL_VALUE) throw new IllegalArgumentException("EVs cannot exceed a total of " + EVs.MAX_TOTAL_VALUE);
    }
}
