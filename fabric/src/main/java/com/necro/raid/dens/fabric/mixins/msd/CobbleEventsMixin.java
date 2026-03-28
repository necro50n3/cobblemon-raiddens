package com.necro.raid.dens.fabric.mixins.msd;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.battles.instruction.FormeChangeEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.MegaEvolutionEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.TerastallizationEvent;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.github.yajatkaul.mega_showdown.event.CobbleEvents;
import com.github.yajatkaul.mega_showdown.gimmick.MegaGimmick;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.IRaidBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CobbleEvents.class)
public class CobbleEventsMixin {
    @Inject(method = "terrastallizationUsed", at = @At("HEAD"), remap = false, cancellable = true)
    private static void terrastallizationUsedInject(TerastallizationEvent event, CallbackInfo ci) {
        PokemonBattle battle = event.getBattle();
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        PokemonEntity pokemon = event.getPokemon().getEntity();
        if (pokemon == null) return;
        else if (!((IRaidAccessor) pokemon).crd_isRaidBoss()) return;
        ci.cancel();
    }

    @Inject(method = "dynamaxStarted", at = @At("HEAD"), remap = false, cancellable = true)
    private static void dynamaxStartedInject(PokemonBattle battle, BattlePokemon battlePokemon, Boolean gmax, CallbackInfo ci) {
        if (!((IRaidBattle) battle).crd_isRaidBattle()) return;
        PokemonEntity pokemon = battlePokemon.getEntity();
        if (pokemon == null) return;
        else if (!((IRaidAccessor) pokemon).crd_isRaidBoss()) return;
        ci.cancel();
    }

    @Inject(method = "megaEvolution", at = @At("HEAD"), remap = false, cancellable = true)
    private static void megaEvolutionInject(MegaEvolutionEvent event, CallbackInfo ci) {
        if (!((IRaidBattle) event.getBattle()).crd_isRaidBattle()) return;
        PokemonEntity pokemon = event.getPokemon().getEntity();
        if (pokemon == null) return;
        else if (!((IRaidAccessor) pokemon).crd_isRaidBoss()) return;
        else if (!MegaGimmick.isMega(pokemon.getPokemon())) return;
        ci.cancel();
    }

    @Inject(method = "formChanged", at = @At("HEAD"), remap = false, cancellable = true)
    private static void formChangedInject(FormeChangeEvent event, CallbackInfo ci) {
        if (!((IRaidBattle) event.getBattle()).crd_isRaidBattle()) return;
        PokemonEntity pokemon = event.getPokemon().getEntity();
        if (pokemon == null) return;
        else if (!((IRaidAccessor) pokemon).crd_isRaidBoss()) return;
        else if (!pokemon.getPokemon().getForm().formOnlyShowdownId().equals(event.getFormeName())) return;
        ci.cancel();
    }
}
