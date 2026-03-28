package com.necro.raid.dens.common.showdown.events;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMusicPacket;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.scripts.triggers.RaidTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RaidEvents {
    public record AddScriptRaidEvent(RaidTrigger<?> trigger) implements RaidEvent {
        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {
            raid.addTrigger(this.trigger);
        }
    }

    public record HealRaidEvent(float ratio) implements RaidEvent {
        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {
            raid.healBoss(this.ratio);
        }
    }

    public record ModifyCatchRateRaidEvent(float mod, String operation) implements RaidEvent {
        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {
            if (player == null) return;
            if ("add".equals(this.operation)) raid.addCatchRate(player, this.mod);
            else if ("multiply".equals(this.operation)) raid.mulCatchRate(player, this.mod);
        }
    }

    public record ReduceTimerRaidEvent(float ratio) implements RaidEvent {
        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {
            raid.reduceTimer(this.ratio);
        }
    }

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
                return this.direction == -1 ? this.scale <= this.targetScale : this.scale >= this.targetScale;
            }
        }
    }

    public record TimerRaidEvent(int time) implements RaidEvent {
        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {
            raid.initTimer(this.time);
        }
    }

    public record ModifyCatchRateAllRaidEvent(float mod, String operation) implements BroadcastingRaidEvent {
        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {
            if (player == null) return;
            if ("add".equals(this.operation)) raid.addCatchRate(player, this.mod);
            else if ("multiply".equals(this.operation)) raid.mulCatchRate(player, this.mod);
        }
    }

    public record PlaySoundRaidEvent(ResourceLocation sound, boolean isMusic) implements BroadcastingRaidEvent {
        @Override
        public void broadcast(RaidInstance raid, Collection<ServerPlayer> players) {
            if (this.isMusic) CobblemonNetwork.sendPacketToPlayers(players, new BattleMusicPacket(this.sound, 1F, 1F, true));
            else raid.getBossEntity().level().playSound(null, raid.getBossEntity().blockPosition(), SoundEvent.createVariableRangeEvent(this.sound), SoundSource.NEUTRAL);
        }

        @Override
        public void run(RaidInstance raid, @Nullable ServerPlayer player) {}
    }
}
