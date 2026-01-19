package com.necro.raid.dens.common.mixins.raid;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.necro.raid.dens.common.CobblemonRaidDens;
import com.necro.raid.dens.common.util.ITransformer;
import kotlin.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BattleActor.class)
public abstract class BattleActorMixin implements ITransformer {
    @Unique
    private BattlePokemon crd_transformTarget = null;

    @Override
    public void crd_setTransformTarget(Pokemon pokemon) {
        CobblemonRaidDens.LOGGER.info("Setting transform target to Pokemon Actor");
        this.crd_transformTarget = new BattlePokemon(pokemon, pokemon, entity -> Unit.INSTANCE);
    }

    @Override
    public BattlePokemon crd_getTransformBattlePokemon() {
        return this.crd_transformTarget;
    }
}
