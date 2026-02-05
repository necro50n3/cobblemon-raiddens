package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.api.events.Cancelable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
