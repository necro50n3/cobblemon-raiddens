package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import com.necro.raid.dens.common.items.item.RaidPouchItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * An event to modify or cancel a player opening a raid pouch and receiving the rewards.
 *
 * @see RaidPouchItem
 */
public class OpenPouchEvent extends Cancelable {
    private final ServerPlayer player;
    private final ItemStack pouch;
    private final List<ItemStack> rewards;

    public OpenPouchEvent(ServerPlayer player, ItemStack pouch, List<ItemStack> rewards) {
        this.player = player;
        this.pouch = pouch;
        this.rewards = rewards;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public ItemStack getPouch() {
        return this.pouch;
    }

    public List<ItemStack> getRewards() {
        return this.rewards;
    }

    public void addReward(ItemStack stack) {
        this.rewards.add(stack);
    }
}
