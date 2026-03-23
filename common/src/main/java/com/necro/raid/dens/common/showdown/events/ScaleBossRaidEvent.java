package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.necro.raid.dens.common.raids.RaidInstance;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record ScaleBossRaidEvent(float scale, float rate) implements RaidEvent {
    private static final List<ScaleData> SCALE_QUEUE = new ArrayList<>();
    @Override
    public void run(RaidInstance raid, @Nullable ServerPlayer player) {
        SCALE_QUEUE.add(new ScaleData(raid.getBossEntity(), this.scale, this.rate));
    }

    public static void tick() {
        SCALE_QUEUE.removeIf(ScaleData::scale);
    }

    private static class ScaleData {
        private final PokemonEntity pokemonEntity;
        private float scale;
        private final float targetScale;
        private final float rate;
        private final int direction;

        private ScaleData(PokemonEntity pokemonEntity, float scale, float rate) {
            this.pokemonEntity = pokemonEntity;
            this.scale = pokemonEntity.getPokemon().getScaleModifier();
            this.targetScale = scale * this.scale;
            this.rate = Math.min(rate, 1F) * (this.targetScale - this.scale);
            this.direction = this.targetScale < this.scale ? -1 : 1;
        }

        private boolean scale() {
            if (this.pokemonEntity == null) return true;
            this.scale += this.rate;
            this.pokemonEntity.getPokemon().setScaleModifier(this.scale);
            return this.isFinished();
        }

        private boolean isFinished() {
            return this.direction == -1 ? this.scale < this.targetScale : this.scale > this.targetScale;
        }
    }
}
