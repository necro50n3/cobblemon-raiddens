package com.necro.raid.dens.common.raids;

import com.necro.raid.dens.common.blocks.entity.RaidCrystalBlockEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class RequestHandler {
    private final RaidCrystalBlockEntity blockEntity;
    private final Map<String, Player> players;

    public RequestHandler(RaidCrystalBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.players = new HashMap<>();
    }

    public RaidCrystalBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public void addPlayer(Player player) {
        this.players.put(player.getName().getString(), player);
    }

    public Player getPlayer(String player) {
        return this.players.get(player);
    }

    public int getPlayerCount() {
        return players.size() + 1;
    }
}
