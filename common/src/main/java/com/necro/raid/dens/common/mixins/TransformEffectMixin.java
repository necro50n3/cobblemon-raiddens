package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.effects.TransformEffect;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.network.RaidDenNetworkMessages;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
    private float scale;

    @Shadow(remap = false)
    private PokemonProperties mock;

    @Inject(method = "<init>(Lcom/cobblemon/mod/common/pokemon/Pokemon;Z)V", at = @At("RETURN"), remap = false)
    private void initInject(Pokemon mimic, boolean canCry, CallbackInfo ci) {
        this.scale = Mth.clamp(80f / mimic.getSpecies().getHeight(), 1.0f, 5.0f);
    }

    @Inject(method = "apply", at = @At("HEAD"), remap = false)
    private void applyInject(PokemonEntity entity, CompletableFuture<PokemonEntity> future, CallbackInfo ci) {
        if (!((IRaidAccessor) entity).isRaidBoss()) return;

        //TODO: Fix aspects not syncing to client
        Set<String> aspects = new HashSet<>(this.mock.getAspects());
        aspects.add("raid");
        this.mock.setAspects(aspects);

        entity.getPokemon().setScaleModifier(this.scale);
        entity.refreshDimensions();
        RaidDenNetworkMessages.RESIZE.accept((ServerLevel) entity.level(), entity, this.scale);
    }

    @Inject(method = "apply", at = @At("RETURN"), remap = false)
    private void applyInject2(PokemonEntity entity, CompletableFuture<PokemonEntity> future, CallbackInfo ci) {
        CobblemonRaidDens.LOGGER.info(String.valueOf(this.mock.getAspects()));
    }
}
