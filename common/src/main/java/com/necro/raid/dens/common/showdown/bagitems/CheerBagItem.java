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

public record CheerBagItem(CheerType cheerType) implements BagItem {

    @Override
    public @NotNull String getItemName() {
        return cheerType.getItemId();
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
        return cheerType.getShowdownString() + " " + data;
    }

    public enum CheerType {
        ATTACK("cheer_attack", "cheer_attack"),
        DEFENSE("cheer_defense", "cheer_defense"),
        HEAL("cheer_heal", "cheer_heal");

        private final String id;
        private final String showdownString;

        CheerType(String id, String showdownString) {
            this.id = id;
            this.showdownString = showdownString;
        }

        public String getId() {
            return this.id;
        }

        public String getItemId() {
            return "item.cobblemonraiddens." + this.id;
        }

        public String getShowdownString() {
            return this.showdownString;
        }
    }
}
