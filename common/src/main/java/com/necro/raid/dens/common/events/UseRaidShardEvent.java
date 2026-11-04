package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import com.necro.raid.dens.common.items.item.RaidShardItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * An event that is fired when a player uses a raid shard with sufficient raid energy.
 * <p>
 * @see RaidShardItem
 */
public class UseRaidShardEvent extends Cancelable {
    private final ServerPlayer player;
    private final ItemStack raidShard;

    public UseRaidShardEvent(ServerPlayer player, ItemStack raidShard) {
        this.player = player;
        this.raidShard = raidShard;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    public ItemStack getRaidShard() {
        return this.raidShard;
    }
}
