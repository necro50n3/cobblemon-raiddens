package com.necro.raid.dens.common.util;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.necro.raid.dens.common.raids.RaidInstance;

import java.util.function.BiConsumer;

public interface IRaidBattle {
    boolean isRaidBattle();
    RaidInstance getRaidBattle();
    void setRaidBattle(RaidInstance raidBattle);
    void addToQueue(BiConsumer<RaidInstance, PokemonBattle> instruction);
}
