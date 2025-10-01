package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.effects.TransformEffect;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(TransformEffect.class)
public abstract class TransformEffectMixin {
    @Shadow(remap = false)
    private PokemonProperties mock;

    @Inject(method = "apply", at = @At("HEAD"), remap = false)
    private void applyInject(PokemonEntity entity, CompletableFuture<PokemonEntity> future, CallbackInfo ci) {
        if (!((IRaidAccessor) entity).isRaidBoss()) return;

        Set<String> aspects = new HashSet<>(this.mock.getAspects());
        aspects.add("raid");
        this.mock.setAspects(aspects);
    }
}
