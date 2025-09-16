package com.necro.raid.dens.common.showdown;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.necro.raid.dens.common.items.item.CheerItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CheerBagItem(CheerItem.CheerType cheerType, String param) implements BagItem {

    public CheerBagItem(CheerItem.CheerType cheerType, int param) {
        this(cheerType, String.valueOf(param));
    }

    public CheerBagItem(CheerItem.CheerType cheerType, double param) {
        this(cheerType, String.valueOf(param));
    }

    @Override
    public @NotNull String getItemName() {
        return cheerType.getItemId();
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
        return cheerType.getShowdownString() + " " + this.param + " " + this.cheerType.getId() + " " + data;
    }

    @Override
    public boolean canStillUse(@NotNull ServerPlayer player, @NotNull PokemonBattle battle, @NotNull BattleActor actor, @NotNull BattlePokemon target, @NotNull ItemStack stack) {
        return false;
    }
}
