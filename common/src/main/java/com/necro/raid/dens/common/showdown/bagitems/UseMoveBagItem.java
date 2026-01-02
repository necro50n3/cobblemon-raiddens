package com.necro.raid.dens.common.showdown.bagitems;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.necro.raid.dens.common.CobblemonRaidDens;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record UseMoveBagItem(String move, int target) implements BagItem {
    @Override
    public @NotNull String getItemName() {
        return "use_move";
    }

    @Override
    public @NotNull Item getReturnItem() {
        return Items.AIR;
    }

    @Override
    public boolean canUse(@NotNull ItemStack stack, @NotNull PokemonBattle battle, @NotNull BattlePokemon target) {
        return target.getHealth() > 0;
    }

    @Override
    public @NotNull String getShowdownInput(@NotNull BattleActor actor, @NotNull BattlePokemon pokemon, @Nullable String data) {
        return String.format("use_move %s %s", this.move, this.target);
    }
}
