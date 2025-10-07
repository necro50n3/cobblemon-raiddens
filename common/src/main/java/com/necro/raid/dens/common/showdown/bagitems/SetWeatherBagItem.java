package com.necro.raid.dens.common.showdown.bagitems;

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

public record SetWeatherBagItem(String weather) implements BagItem {
    @Override
    public @NotNull String getItemName() {
        return "set_weather";
    }

    @Override
    public @NotNull Item getReturnItem() {
        return Items.AIR;
    }

    @Override
    public boolean canUse(@NotNull PokemonBattle battle, @NotNull BattlePokemon target) {
        return target.getHealth() > 0;
    }

    @Override
    public @NotNull String getShowdownInput(@NotNull BattleActor actor, @NotNull BattlePokemon pokemon, @Nullable String data) {
        return String.format("set_weather %s", this.weather);
    }

    @Override
    public boolean canStillUse(@NotNull ServerPlayer player, @NotNull PokemonBattle battle, @NotNull BattleActor actor, @NotNull BattlePokemon pokemon, @NotNull ItemStack stack) {
        return false;
    }
}
