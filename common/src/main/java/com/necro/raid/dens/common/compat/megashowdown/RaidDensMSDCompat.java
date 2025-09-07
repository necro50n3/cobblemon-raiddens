package com.necro.raid.dens.common.compat.megashowdown;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.raids.RaidType;
import net.minecraft.world.item.ItemStack;

public abstract class RaidDensMSDCompat {
    public static RaidDensMSDCompat INSTANCE;

    public abstract void setupTera(PokemonEntity pokemonEntity, Pokemon pokemon);

    public abstract void setupDmax(PokemonEntity pokemonEntity);

    public abstract ItemStack getTeraShard(RaidType raidType);

    public abstract ItemStack getMaxMushroom();
}
