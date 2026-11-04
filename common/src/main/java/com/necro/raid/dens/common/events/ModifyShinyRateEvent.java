package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;

/**
 * An event to modify the shiny rate for a specific player when catching the raid boss.
 */
public class ModifyShinyRateEvent {
    private final ServerPlayer player;
    private final Pokemon reward;
    private float shinyRate;

    public ModifyShinyRateEvent(ServerPlayer player, Pokemon reward, float shinyRate) {
        this.player = player;
        this.reward = reward;
        this.shinyRate = shinyRate;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public Pokemon reward() {
        return this.reward;
    }

    public float shinyRate() {
        return this.shinyRate;
    }

    public void set(float rate) {
        this.shinyRate = rate;
    }
}
