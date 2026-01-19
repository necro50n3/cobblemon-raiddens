package com.necro.raid.dens.common.mixins.showdown;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.entity.pokemon.effects.TransformEffect;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.util.IRaidAccessor;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(TransformEffect.class)
public abstract class TransformEffectMixin {
    @Unique
    private float crd_raidScale;

    @Shadow(remap = false)
    private PokemonProperties mock;

    @Inject(method = "<init>(Lcom/cobblemon/mod/common/pokemon/Pokemon;Z)V", at = @At("RETURN"), remap = false)
    private void initInject(Pokemon mimic, boolean canCry, CallbackInfo ci) {
        this.crd_raidScale = Mth.clamp(80f / mimic.getSpecies().getHeight(), 1.0f, 5.0f);
    }

    @Inject(method = "apply", at = @At("HEAD"), remap = false)
    private void applyInject(PokemonEntity entity, CompletableFuture<PokemonEntity> future, CallbackInfo ci) {
        if (!((IRaidAccessor) entity).crd_isRaidBoss()) return;

        Set<String> aspects = new HashSet<>(this.mock.getAspects());
        aspects.add("raid");
        this.mock.setAspects(aspects);

        if (this.crd_raidScale > 0) entity.getPokemon().setScaleModifier(this.crd_raidScale);
        entity.refreshDimensions();
    }
}
