package com.necro.raid.dens.fabric.mixins.msd;

import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.github.yajatkaul.mega_showdown.api.lilycobble.networking.battle.BattlePokemonState;
import com.llamalad7.mixinextras.sugar.Local;
import com.necro.raid.dens.common.raids.RaidInstance;
import com.necro.raid.dens.common.raids.battle.RaidBattleState;
import com.necro.raid.dens.common.raids.helpers.RaidHelper;
import com.necro.raid.dens.common.util.IRaidAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(BattlePokemonState.class)
public class BattlePokemonStateMixin {
    @Inject(
        method = "of",
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/yajatkaul/mega_showdown/api/lilycobble/networking/battle/BattlePokemonState;<init>(Ljava/util/UUID;Ljava/util/Optional;DLjava/util/Optional;Ljava/util/Optional;Ljava/util/Map;Ljava/util/Optional;)V"
        )
    )
    private static void modifyStatChanges(BattlePokemon pokemon, boolean includeProperties, boolean includeAbility, boolean includeMoves, boolean includeItem, CallbackInfoReturnable<BattlePokemonState> cir, @Local Map<String, Integer> statChanges) {
        if (pokemon.getEntity() == null) return;
        else if (!((IRaidAccessor) pokemon.getEntity()).crd_isRaidBoss()) return;
        statChanges.clear();
        RaidInstance raid = RaidHelper.ACTIVE_RAIDS.get(((IRaidAccessor) pokemon.getEntity()).crd_getRaidId());
        if (raid == null) return;
        RaidBattleState battleState = raid.getBattleState();
        battleState.bossSide.pokemon.boosts.forEach((stat, boost) ->
            statChanges.put(stat.getShowdownId(), boost)
        );
    }
}
