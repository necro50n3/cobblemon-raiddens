package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.ITransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BattleRegistry.class)
public class BattleRegistryMixin {
    @WrapOperation(
        method = "startShowdown",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/battles/BattleRegistry;packTeam(Ljava/util/List;)Ljava/lang/String;"
        )
    )
    private String packTeamInject(BattleRegistry instance, List<BattlePokemon> pokemonList, Operation<String> original) {
        BattleActor actor = pokemonList.getFirst().getActor();
        BattlePokemon transformTarget = ((ITransformer) actor).crd_getTransformBattlePokemon();
        if (transformTarget != null) CobblemonRaidDens.LOGGER.info("Getting team from transform target");
        else CobblemonRaidDens.LOGGER.info("Transform target is null. Getting default");
        return original.call(instance, transformTarget == null ? pokemonList : List.of(transformTarget));
    }
}
