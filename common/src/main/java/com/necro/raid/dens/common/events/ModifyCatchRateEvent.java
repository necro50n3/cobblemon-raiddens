package com.necro.raid.dens.common.events;

import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class ModifyCatchRateEvent {
    private final ServerPlayer player;
    private final Pokemon reward;
    private float catchRate;

    public ModifyCatchRateEvent(ServerPlayer player, Pokemon reward, float catchRate) {
        this.player = player;
        this.reward = reward;
        this.catchRate = catchRate;
    }

    public ServerPlayer player() {
        return this.player;
    }

    public Pokemon reward() {
        return this.reward;
    }

    public float catchRate() {
        return this.catchRate;
    }

    public void multiply(float mod) {
        this.catchRate = Mth.clamp(this.catchRate * mod, 0F, 1F);
    }

    public void add(float mod) {
        this.catchRate = Mth.clamp(this.catchRate + mod, 0F, 1F);
    }
}
