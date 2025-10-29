package com.necro.raid.dens.common.showdown.bagitems;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ClearBoostBagItem(ClearType clearType) implements BagItem {
    @Override
    public @NotNull String getItemName() {
        return "clear_boost";
    }

    @Override
    public @NotNull Item getReturnItem() {
        return Items.AIR;
    }

    @Override
    public boolean canUse(@NotNull ItemStack stack, @NotNull PokemonBattle pokemonBattle, @NotNull BattlePokemon battlePokemon) {
        return true;
    }

    @Override
    public @NotNull String getShowdownInput(@NotNull BattleActor battleActor, @NotNull BattlePokemon battlePokemon, @Nullable String data) {
        return this.clearType.getId() + " " + data;
    }

    public enum ClearType {
        BOSS("clear_boss"),
        PLAYER("clear_player");

        private final String id;

        ClearType(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }
}
