package com.necro.raid.dens.common.util;

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;

public interface ITransformer {
    void crd_setTransformTarget(Pokemon pokemon);
    default Pokemon crd_getTransformTarget() {
        return null;
    }
    default BattlePokemon crd_getTransformBattlePokemon() {
        return null;
    }
}
