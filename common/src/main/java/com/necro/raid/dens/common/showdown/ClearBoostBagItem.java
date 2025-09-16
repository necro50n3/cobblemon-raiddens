package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClearBoostBagItem implements BagItem {
    @Override
    public @NotNull String getItemName() {
        return "clear_boost";
    }

    @Override
    public @NotNull Item getReturnItem() {
        return Items.AIR;
    }

    @Override
    public boolean canUse(@NotNull PokemonBattle pokemonBattle, @NotNull BattlePokemon battlePokemon) {
        return true;
    }

    @Override
    public @NotNull String getShowdownInput(@NotNull BattleActor battleActor, @NotNull BattlePokemon battlePokemon, @Nullable String s) {
        return "clear_boost";
    }

    @Override
    public boolean canStillUse(@NotNull ServerPlayer serverPlayer, @NotNull PokemonBattle pokemonBattle, @NotNull BattleActor battleActor, @NotNull BattlePokemon battlePokemon, @NotNull ItemStack itemStack) {
        return true;
    }
}
