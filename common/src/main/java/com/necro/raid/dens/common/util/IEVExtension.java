package com.necro.raid.dens.common.util;

import com.cobblemon.mod.common.api.pokemon.stats.Stat;

public interface IEVExtension {
    void crd_forceSet(Stat stat, int value);
    void crd_validate();
}
