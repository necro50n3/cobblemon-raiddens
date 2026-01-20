package com.necro.raid.dens.fabric.mixins.msd;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.events.battles.instruction.TerastallizationEvent;
import com.github.yajatkaul.mega_showdown.event.CobbleEvents;
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
        else if (event.getPokemon().getEntity() == null) return;
        else if (!((IRaidAccessor) event.getPokemon().getEntity()).crd_isRaidBoss()) return;
        else if (!((IRaidAccessor) event.getPokemon().getEntity()).crd_getRaidBoss().isTera()) return;
        ci.cancel();
    }
}
