package com.necro.raid.dens.common.mixins;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.MoveActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.battles.ShowdownMoveset;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.exception.IllegalActionChoiceException;
import com.necro.raid.dens.common.util.IRaidAccessor;
import com.necro.raid.dens.common.util.RaidUtils;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MoveActionResponse.class)
public abstract class MoveActionResponseMixin {
    @Shadow(remap = false)
    private String moveName;

    @Inject(method = "isValid", at = @At("HEAD"), remap = false)
    private void isValidInject(ActiveBattlePokemon activeBattlePokemon, ShowdownMoveset showdownMoveSet, boolean forceSwitch, CallbackInfoReturnable<Boolean> cir) {
        List<ActiveBattlePokemon> targetPokemon = activeBattlePokemon.getActor().getBattle().getSide2().getActivePokemon();
        if (targetPokemon.isEmpty()) return;
        else if (targetPokemon.contains(activeBattlePokemon)) return;
        BattlePokemon battlePokemon = targetPokemon.getFirst().getBattlePokemon();
        if (battlePokemon == null) return;
        PokemonEntity pokemonEntity = battlePokemon.getEntity();
        if (pokemonEntity == null) return;
        else if (!((IRaidAccessor) pokemonEntity).isRaidBoss()) return;
        else if (!RaidUtils.isMoveBlacklisted(this.moveName)) return;

        BattleActor battleActor = activeBattlePokemon.getActor();
        List<ShowdownActionResponse> originalActions = battleActor.getExpectingPassActions().stream().toList();
        battleActor.getExpectingPassActions().clear();
        battleActor.getExpectingPassActions().addAll(originalActions);
        throw new IllegalActionChoiceException(
            battleActor,
            Component.translatable(
                "message.cobblemonraiddens.raid.forbidden_move",
                Component.translatable("cobblemon.move." + this.moveName)
            ).getString()
        );
    }
}
